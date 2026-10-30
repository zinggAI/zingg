package io.zingg.duckdb.engine;

public final class SimilarityFunctions {
 private SimilarityFunctions(){}
 public static double exact(String a,String b){return a==null||b==null?0d:(a.equals(b)?1d:0d);}
 /** Legacy released profile: blank values intentionally use the historical safe score of 1. */
 public static double jaccard(String a,String b){if(isBlank(a)||isBlank(b))return 1d;return tokenJaccard(a,b,1d);}
 /** Exact Zingg v0.7 StringSimilarityDistanceFunction + SecondString Jaccard behavior. */
 public static double secondStringJaccard(String a,String b){
  if(a==null||a.isEmpty()||b==null||b.isEmpty()||a.equals(b))return 1d;
  return tokenJaccard(a,b,0d);
 }
 private static double tokenJaccard(String a,String b,double emptyTokenSetScore){
  var left=tokens(a);var right=tokens(b);var union=new java.util.HashSet<>(left);union.addAll(right);
  if(union.isEmpty())return emptyTokenSetScore;
  var intersection=new java.util.HashSet<>(left);intersection.retainAll(right);
  return (double)intersection.size()/union.size();
 }
 private static boolean isBlank(String value){return value==null||value.trim().isEmpty();}
 private static java.util.Set<String> tokens(String value){
  var result=new java.util.HashSet<String>();var token=new StringBuilder();int kind=0;
  for(int i=0;i<value.length();i++){
   char c=value.charAt(i);int next=Character.isLetter(c)?1:Character.isDigit(c)?2:0;
   if(next==0){addToken(result,token);kind=0;continue;}
   if(kind!=0&&kind!=next)addToken(result,token);
   token.append(c);kind=next;
  }
  addToken(result,token);return result;
 }
 private static void addToken(java.util.Set<String> tokens,StringBuilder token){
  if(token.length()>0){tokens.add(token.toString().toLowerCase());token.setLength(0);}
 }
 public static double normalizedLevenshtein(String a,String b){if(a==null||b==null)return 0d;if(a.equals(b))return 1d;int[] prev=new int[b.length()+1],cur=new int[b.length()+1];for(int j=0;j<=b.length();j++)prev[j]=j;for(int i=1;i<=a.length();i++){cur[0]=i;for(int j=1;j<=b.length();j++)cur[j]=Math.min(Math.min(cur[j-1]+1,prev[j]+1),prev[j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));int[] t=prev;prev=cur;cur=t;}return 1d-(double)prev[b.length()]/Math.max(a.length(),b.length());}
 /** Strict v0.7 compatibility: the released JaroWinkler path delegates to plain Jaro. */
 public static double jaro(String a,String b){
  if(a==null||b==null)return 0d; if(a.equals(b))return 1d;
  int leftLength=a.length(), rightLength=b.length(); if(leftLength==0||rightLength==0)return 0d;
  int distance=Math.max(leftLength,rightLength)/2-1; boolean[] left=new boolean[leftLength],right=new boolean[rightLength]; int matches=0;
  for(int i=0;i<leftLength;i++){int start=Math.max(0,i-distance),end=Math.min(i+distance+1,rightLength);for(int j=start;j<end;j++)if(!right[j]&&a.charAt(i)==b.charAt(j)){left[i]=true;right[j]=true;matches++;break;}}
  if(matches==0)return 0d; int transpositions=0,j=0; for(int i=0;i<leftLength;i++)if(left[i]){while(!right[j])j++;if(a.charAt(i)!=b.charAt(j))transpositions++;j++;}
  return ((double)matches/leftLength+(double)matches/rightLength+(matches-transpositions/2.0)/matches)/3.0;
 }
}
