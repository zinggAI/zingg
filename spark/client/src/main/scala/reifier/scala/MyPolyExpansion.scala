package zingg.scala

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.spark.ml.feature._

import scala.collection.mutable

import org.apache.spark.annotation.Experimental
import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.param.{ParamMap, IntParam, ParamValidators}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.mllib.linalg._
import org.apache.spark.sql.types.DataType

/**
 * :: Experimental ::
 * Perform feature expansion in a polynomial space. As said in wikipedia of Polynomial Expansion,
 * which is available at [[http://en.wikipedia.org/wiki/Polynomial_expansion]], "In mathematics, an
 * expansion of a product of sums expresses it as a sum of products by using the fact that
 * multiplication distributes over addition". Take a 2-variable feature vector as an example:
 * `(x, y)`, if we want to expand it with degree 2, then we get `(x, x * x, y, x * y, y * y)`.
 */

@Experimental
class MyPolyExpansion(override val uid: String)
  extends UnaryTransformer[Vector, Vector, MyPolyExpansion] {

  def this() = this(Identifiable.randomUID("poly"))

  /**
   * The polynomial degree to expand, which should be >= 1.  A value of 1 means no expansion.
   * Default: 2
   * @group param
   */
  val degree = new IntParam(this, "degree", "the polynomial degree to expand (>= 1)",
    ParamValidators.gt(1))

  setDefault(degree -> 2)

  /** @group getParam */
  def getDegree: Int = $(degree)

  /** @group setParam */
  def setDegree(value: Int): this.type = set(degree, value)

  override protected def createTransformFunc: Vector => Vector = { v =>
    MyPolyExpansion.expand(v, $(degree))
  }

  override protected def outputDataType: DataType = new VectorUDT()

  override def copy(extra: ParamMap): MyPolyExpansion = defaultCopy(extra)
}

/**
 * The expansion is done via recursion. Given n features and degree d, the size after expansion is
 * (n + d choose d) (including 1 and first-order values). For example, let f([a, b, c], 3) be the
 * function that expands [a, b, c] to their monomials of degree 3. We have the following recursion:
 *
 * {{{
 * f([a, b, c], 3) = f([a, b], 3) ++ f([a, b], 2) * c ++ f([a, b], 1) * c^2 ++ [c^3]
 * }}}
 *
 * To handle sparsity, if c is zero, we can skip all monomials that contain it. We remember the
 * current index and increment it properly for sparse input.
 */
private object MyPolyExpansion {

  private def choose(n: Int, k: Int): Int = {
    Range(n, n - k, -1).product / Range(k, 1, -1).product
  }

  private def getPolySize(numFeatures: Int, degree: Int): Int = choose(numFeatures + degree, degree)

  private def expandDense(
      values: Array[Double],
      lastIdx: Int,
      degree: Int,
      multiplier: Double,
      polyValues: Array[Double],
      curPolyIdx: Int): Int = {
    if (multiplier == 0.0) {
      // do nothing
    } else if (degree == 0 || lastIdx < 0) {
      if (curPolyIdx >= 0) { // skip the very first 1
        polyValues(curPolyIdx) = multiplier
      }
    } else {
      val v = values(lastIdx)
      val lastIdx1 = lastIdx - 1
      var alpha = multiplier
      var i = 0
      var curStart = curPolyIdx
      while (i <= degree && alpha != 0.0) {
        curStart = expandDense(values, lastIdx1, degree - i, alpha, polyValues, curStart)
        i += 1
        alpha *= v
      }
    }
    curPolyIdx + getPolySize(lastIdx + 1, degree)
  }

  private def expandSparse(
      indices: Array[Int],
      values: Array[Double],
      lastIdx: Int,
      lastFeatureIdx: Int,
      degree: Int,
      multiplier: Double,
      polyIndices: mutable.ArrayBuilder[Int],
      polyValues: mutable.ArrayBuilder[Double],
      curPolyIdx: Int): Int = {
    if (multiplier == 0.0) {
      // do nothing
    } else if (degree == 0 || lastIdx < 0) {
      if (curPolyIdx >= 0) { // skip the very first 1
        polyIndices += curPolyIdx
        polyValues += multiplier
      }
    } else {
      // Skip all zeros at the tail.
      val v = values(lastIdx)
      val lastIdx1 = lastIdx - 1
      val lastFeatureIdx1 = indices(lastIdx) - 1
      var alpha = multiplier
      var curStart = curPolyIdx
      var i = 0
      while (i <= degree && alpha != 0.0) {
        curStart = expandSparse(indices, values, lastIdx1, lastFeatureIdx1, degree - i, alpha,
          polyIndices, polyValues, curStart)
        i += 1
        alpha *= v
      }
    }
    curPolyIdx + getPolySize(lastFeatureIdx + 1, degree)
  }

  private def expand(dv: DenseVector, degree: Int): DenseVector = {
    val n = dv.size
    val polySize = getPolySize(n, degree)
    val polyValues = new Array[Double](polySize - 1)
    expandDense(dv.values, n - 1, degree, 1.0, polyValues, -1)
    new DenseVector(polyValues)
  }

  private def expand(sv: SparseVector, degree: Int): SparseVector = {
    val polySize = getPolySize(sv.size, degree)
    val nnz = sv.values.length
    val nnzPolySize = getPolySize(nnz, degree)
    val polyIndices = mutable.ArrayBuilder.make[Int]
    polyIndices.sizeHint(nnzPolySize - 1)
    val polyValues = mutable.ArrayBuilder.make[Double]
    polyValues.sizeHint(nnzPolySize - 1)
    expandSparse(
      sv.indices, sv.values, nnz - 1, sv.size - 1, degree, 1.0, polyIndices, polyValues, -1)
    new SparseVector(polySize - 1, polyIndices.result(), polyValues.result())
  }

  def expand(v: Vector, degree: Int): Vector = {
    v match {
      case dv: DenseVector => expand(dv, degree)
      case sv: SparseVector => expand(sv, degree)
      case _ => throw new IllegalArgumentException
    }
  }
}
