::: wy-grid-for-nav
::: wy-side-scroll
::: wy-side-nav-search
[Zingg Enterprise](index.html){.icon .icon-home}

::: {role="search"}
:::
:::

::: {.wy-menu .wy-menu-vertical spy="affix" role="navigation" aria-label="Navigation menu"}
-   [Zingg Enterpise Entity Resolution Package](#){.current .reference
    .internal}
    -   [zinggES.enterprise.spark.ESparkClient](#zingges-enterprise-spark-esparkclient){.reference
        .internal}
    -   [[`EZingg`{.docutils .literal
        .notranslate}]{.pre}](#zinggES.enterprise.spark.ESparkClient.EZingg){.reference
        .internal}
    -   [[`EZinggWithSpark`{.docutils .literal
        .notranslate}]{.pre}](#zinggES.enterprise.spark.ESparkClient.EZinggWithSpark){.reference
        .internal}
:::
:::

::: {.section .wy-nav-content-wrap toggle="wy-nav-shift"}
[Zingg Enterprise](index.html)

::: wy-nav-content
::: rst-content
::: {role="navigation" aria-label="Page navigation"}
-   [](index.html){.icon .icon-home aria-label="Home"}
-   Zingg Enterpise Entity Resolution Package
-   [View page source](_sources/zinggES.rst.txt){rel="nofollow"}

------------------------------------------------------------------------
:::

::: {.document role="main" itemscope="itemscope" itemtype="http://schema.org/Article"}
::: {itemprop="articleBody"}
::: {#zingg-enterpise-entity-resolution-package .section}
# Zingg Enterpise Entity Resolution Package[](#zingg-enterpise-entity-resolution-package "Link to this heading"){.headerlink}

Zingg Enterprise Python APIs for entity resolution, record linkage, data
mastering and deduplication using ML
([https://www.zingg.ai](https://www.zingg.ai){.reference .external})

requires python 3.6+; spark 3.5.0 Otherwise,
[[`zinggES.enterprise.spark.ESparkClient()`{.xref .py .py-func .docutils
.literal
.notranslate}]{.pre}](#module-zinggES.enterprise.spark.ESparkClient "zinggES.enterprise.spark.ESparkClient"){.reference
.internal} cannot be executed

::: {.toctree-wrapper .compound}
:::

::: {#zingges-enterprise-spark-esparkclient .section}
[]{#module-zinggES.enterprise.spark.ESparkClient}[]{#module-zinggES}

## zinggES.enterprise.spark.ESparkClient[](#zingges-enterprise-spark-esparkclient "Link to this heading"){.headerlink}

This module is the main entry point of the Zingg Enterprise Python API
:::

*[[class]{.pre}]{.k}[ ]{.w}*[[zinggES.enterprise.spark.ESparkClient.]{.pre}]{.sig-prename .descclassname}[[EZingg]{.pre}]{.sig-name .descname}[(]{.sig-paren}*[[args]{.pre}]{.n}*, *[[options]{.pre}]{.n}*[)]{.sig-paren}[[[\[source\]]{.pre}]{.viewcode-link}](_modules/zinggES/enterprise/spark/ESparkClient.html#EZingg){.reference .internal}[](#zinggES.enterprise.spark.ESparkClient.EZingg "Link to this definition"){.headerlink}

:   Bases: [`Zingg`{.xref .py .py-class .docutils .literal
    .notranslate}]{.pre}

    This class is the main point of interface with the Zingg Enterprise
    matching product. Construct a client to Zingg using provided
    arguments and spark master. If running locally, set the master to
    local. This creates a new session.

    Parameters[:]{.colon}

    :   -   **args** (*EArguments*) -- arguments for training and
            matching

        -   **options** (*ClientOptions*) -- client option for this
            class object

```{=html}
<!-- -->
```

*[[class]{.pre}]{.k}[ ]{.w}*[[zinggES.enterprise.spark.ESparkClient.]{.pre}]{.sig-prename .descclassname}[[EZinggWithSpark]{.pre}]{.sig-name .descname}[(]{.sig-paren}*[[args]{.pre}]{.n}*, *[[options]{.pre}]{.n}*[)]{.sig-paren}[[[\[source\]]{.pre}]{.viewcode-link}](_modules/zinggES/enterprise/spark/ESparkClient.html#EZinggWithSpark){.reference .internal}[](#zinggES.enterprise.spark.ESparkClient.EZinggWithSpark "Link to this definition"){.headerlink}

:   Bases: [[`EZingg`{.xref .py .py-class .docutils .literal
    .notranslate}]{.pre}](#zinggES.enterprise.spark.ESparkClient.EZingg "zinggES.enterprise.spark.ESparkClient.EZingg"){.reference
    .internal}

    This class is the main point of interface with the Zingg Enterprise
    matching product. Construct a client to Zingg using provided
    arguments and spark master. If running locally, set the master to
    local and this uses the current spark session.

    Parameters[:]{.colon}

    :   -   **args** (*EArguments*) -- arguments for training and
            matching

        -   **options** (*ClientOptions*) -- client option for this
            class object
:::
:::
:::

::: {.rst-footer-buttons role="navigation" aria-label="Footer"}
[[]{.fa .fa-arrow-circle-left aria-hidden="true"}
Previous](index.html "Zingg Enterpise Entity Resolution Python Package"){.btn
.btn-neutral .float-left accesskey="p" rel="prev"}
:::

------------------------------------------------------------------------

::: {role="contentinfo"}
© Copyright 2025, Zingg.AI.
:::

Built with [Sphinx](https://www.sphinx-doc.org/) using a
[theme](https://github.com/readthedocs/sphinx_rtd_theme) provided by
[Read the Docs](https://readthedocs.org).
:::
:::
:::
:::
