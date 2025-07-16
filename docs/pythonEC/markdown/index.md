::: wy-grid-for-nav
::: wy-side-scroll
::: wy-side-nav-search
[Zingg Enterprise](#){.icon .icon-home}

::: {role="search"}
:::
:::

::: {.wy-menu .wy-menu-vertical spy="affix" role="navigation" aria-label="Navigation menu"}
-   [Zingg Enterpise Entity Resolution Package](zinggEC.html){.reference
    .internal}
:::
:::

::: {.section .wy-nav-content-wrap toggle="wy-nav-shift"}
[Zingg Enterprise](#)

::: wy-nav-content
::: rst-content
::: {role="navigation" aria-label="Page navigation"}
-   [](#){.icon .icon-home aria-label="Home"}
-   Zingg Enterpise Entity Resolution Python Package
-   [View page source](_sources/index.rst.txt){rel="nofollow"}

------------------------------------------------------------------------
:::

::: {.document role="main" itemscope="itemscope" itemtype="http://schema.org/Article"}
::: {itemprop="articleBody"}
::: {#zingg-enterpise-entity-resolution-python-package .section}
# Zingg Enterpise Entity Resolution Python Package[](#zingg-enterpise-entity-resolution-python-package "Link to this heading"){.headerlink}

Zingg Enterprise Python APIs for entity resolution, identity resolution,
record linkage, data mastering and deduplication using ML
([https://www.zingg.ai](https://www.zingg.ai){.reference .external})

::: {.admonition .note}
Note

Requires python 3.6+; spark 3.5.0 Otherwise,
[`zinggES.enterprise.spark.ESparkClient()`{.xref .py .py-func .docutils
.literal .notranslate}]{.pre} cannot be executed
:::

::: {.toctree-wrapper .compound}
-   [Zingg Enterpise Entity Resolution Package](zinggEC.html){.reference
    .internal}
    -   [zinggEC.enterprise.common.ApproverArguments](zinggEC.html#zinggec-enterprise-common-approverarguments){.reference
        .internal}
    -   [[`ApproverArguments`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.ApproverArguments.ApproverArguments){.reference
        .internal}
        -   [[`ApproverArguments.getApprovalQuery()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.getApprovalQuery){.reference
            .internal}
        -   [[`ApproverArguments.getArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.getArgs){.reference
            .internal}
        -   [[`ApproverArguments.getDestination()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.getDestination){.reference
            .internal}
        -   [[`ApproverArguments.getParentArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.getParentArgs){.reference
            .internal}
        -   [[`ApproverArguments.setApprovalQuery()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.setApprovalQuery){.reference
            .internal}
        -   [[`ApproverArguments.setArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.setArgs){.reference
            .internal}
        -   [[`ApproverArguments.setDestination()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.setDestination){.reference
            .internal}
        -   [[`ApproverArguments.setParentArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.setParentArgs){.reference
            .internal}
    -   [zinggEC.enterprise.common.IncrementalArguments](zinggEC.html#zinggec-enterprise-common-incrementalarguments){.reference
        .internal}
    -   [[`IncrementalArguments`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments){.reference
        .internal}
        -   [[`IncrementalArguments.getArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getArgs){.reference
            .internal}
        -   [[`IncrementalArguments.getDeleteAction()`{.docutils
            .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getDeleteAction){.reference
            .internal}
        -   [[`IncrementalArguments.getDeletedData()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getDeletedData){.reference
            .internal}
        -   [[`IncrementalArguments.getIncrementalData()`{.docutils
            .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getIncrementalData){.reference
            .internal}
        -   [[`IncrementalArguments.getParentArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getParentArgs){.reference
            .internal}
        -   [[`IncrementalArguments.setArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setArgs){.reference
            .internal}
        -   [[`IncrementalArguments.setDeleteAction()`{.docutils
            .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setDeleteAction){.reference
            .internal}
        -   [[`IncrementalArguments.setDeletedData()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setDeletedData){.reference
            .internal}
        -   [[`IncrementalArguments.setIncrementalData()`{.docutils
            .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setIncrementalData){.reference
            .internal}
        -   [[`IncrementalArguments.setParentArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setParentArgs){.reference
            .internal}
    -   [zinggEC.enterprise.common.MappingMatchType](zinggEC.html#zinggec-enterprise-common-mappingmatchtype){.reference
        .internal}
    -   [[`MappingMatchType`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.MappingMatchType.MappingMatchType){.reference
        .internal}
        -   [[`MappingMatchType.getMappingMatchType()`{.docutils
            .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.MappingMatchType.MappingMatchType.getMappingMatchType){.reference
            .internal}
    -   [zinggEC.enterprise.common.epipes](zinggEC.html#zinggec-enterprise-common-epipes){.reference
        .internal}
    -   [[`ECsvPipe`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.ECsvPipe){.reference
        .internal}
        -   [[`ECsvPipe.setDelimiter()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.ECsvPipe.setDelimiter){.reference
            .internal}
        -   [[`ECsvPipe.setHeader()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.ECsvPipe.setHeader){.reference
            .internal}
        -   [[`ECsvPipe.setLocation()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.ECsvPipe.setLocation){.reference
            .internal}
    -   [[`EPipe`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.EPipe){.reference
        .internal}
        -   [[`EPipe.getPassthroughExpr()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.EPipe.getPassthroughExpr){.reference
            .internal}
        -   [[`EPipe.getPassthruData()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.EPipe.getPassthruData){.reference
            .internal}
        -   [[`EPipe.getUsableData()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.EPipe.getUsableData){.reference
            .internal}
        -   [[`EPipe.hasPassThru()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.EPipe.hasPassThru){.reference
            .internal}
        -   [[`EPipe.setPassthroughExpr()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.EPipe.setPassthroughExpr){.reference
            .internal}
    -   [[`InMemoryPipe`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.InMemoryPipe){.reference
        .internal}
        -   [[`InMemoryPipe.getDataset()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.InMemoryPipe.getDataset){.reference
            .internal}
        -   [[`InMemoryPipe.setDataset()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.InMemoryPipe.setDataset){.reference
            .internal}
    -   [[`UCPipe`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.UCPipe){.reference
        .internal}
        -   [[`UCPipe.setTable()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.epipes.UCPipe.setTable){.reference
            .internal}
    -   [zinggEC.enterprise.common.EArguments](zinggEC.html#zinggec-enterprise-common-earguments){.reference
        .internal}
    -   [[`DeterministicMatching`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.DeterministicMatching){.reference
        .internal}
        -   [[`DeterministicMatching.getDeterministicMatching()`{.docutils
            .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.DeterministicMatching.getDeterministicMatching){.reference
            .internal}
    -   [[`EArguments`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments){.reference
        .internal}
        -   [[`EArguments.getArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.getArgs){.reference
            .internal}
        -   [[`EArguments.getData()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.getData){.reference
            .internal}
        -   [[`EArguments.getDeterministicMatching()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.getDeterministicMatching){.reference
            .internal}
        -   [[`EArguments.getFieldDefinition()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.getFieldDefinition){.reference
            .internal}
        -   [[`EArguments.getPassthroughExpr()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.getPassthroughExpr){.reference
            .internal}
        -   [[`EArguments.getPrimaryKey()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.getPrimaryKey){.reference
            .internal}
        -   [[`EArguments.setArgs()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.setArgs){.reference
            .internal}
        -   [[`EArguments.setBlockingModel()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.setBlockingModel){.reference
            .internal}
        -   [[`EArguments.setData()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.setData){.reference
            .internal}
        -   [[`EArguments.setDeterministicMatchingCondition()`{.docutils
            .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.setDeterministicMatchingCondition){.reference
            .internal}
        -   [[`EArguments.setFieldDefinition()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.setFieldDefinition){.reference
            .internal}
        -   [[`EArguments.setPassthroughExpr()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EArguments.EArguments.setPassthroughExpr){.reference
            .internal}
    -   [zinggEC.enterprise.common.EFieldDefinition](zinggEC.html#zinggec-enterprise-common-efielddefinition){.reference
        .internal}
    -   [[`EFieldDefinition`{.docutils .literal
        .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition){.reference
        .internal}
        -   [[`EFieldDefinition.getMatchTypeArray()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition.getMatchTypeArray){.reference
            .internal}
        -   [[`EFieldDefinition.getPrimaryKey()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition.getPrimaryKey){.reference
            .internal}
        -   [[`EFieldDefinition.setPrimaryKey()`{.docutils .literal
            .notranslate}]{.pre}](zinggEC.html#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition.setPrimaryKey){.reference
            .internal}
:::
:::

::: {#api-reference .section}
# API Reference[](#api-reference "Link to this heading"){.headerlink}

-   [[Module Index]{.std .std-ref}](py-modindex.html){.reference
    .internal}

-   [[Index]{.std .std-ref}](genindex.html){.reference .internal}

-   [[Search Page]{.std .std-ref}](search.html){.reference .internal}
:::

::: {#example-api-usage .section}
# Example API Usage[](#example-api-usage "Link to this heading"){.headerlink}

::: {.highlight-python .notranslate}
::: highlight
     1from zingg.client import *
     2from zingg.pipes import *
     3from zinggEC.enterprise.common.ApproverArguments import *
     4from zinggEC.enterprise.common.IncrementalArguments import *
     5from zinggEC.enterprise.common.MappingMatchType import *
     6from zinggEC.enterprise.common.epipes import *
     7from zinggEC.enterprise.common.EArguments import *
     8from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
     9from zinggES.enterprise.spark.ESparkClient import *
    10import os
    11
    12#build the arguments for zingg
    13args = EArguments()
    14#set field definitions
    15recId = EFieldDefinition("recId", "string", MatchType.DONT_USE)
    16recId.setPrimaryKey(True)
    17fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
    18# for mapping match type
    19#fname = EFieldDefinition("fname", "string", MatchType.FUZZY, MappingMatchType("MAPPING", "NICKNAMES_TEST"))
    20lname = EFieldDefinition("lname", "string", MatchType.FUZZY)
    21stNo = EFieldDefinition("stNo", "string", MatchType.FUZZY)
    22add1 = EFieldDefinition("add1","string", MatchType.FUZZY)
    23add2 = EFieldDefinition("add2", "string", MatchType.FUZZY)
    24city = EFieldDefinition("city", "string", MatchType.FUZZY)
    25areacode = EFieldDefinition("areacode", "string", MatchType.FUZZY)
    26state = EFieldDefinition("state", "string", MatchType.FUZZY)
    27dob = EFieldDefinition("dob", "string", MatchType.FUZZY)
    28ssn = EFieldDefinition("ssn", "string", MatchType.FUZZY)
    29
    30fieldDefs = [recId, fname, lname, stNo, add1, add2, city, areacode, state, dob, ssn]
    31args.setFieldDefinition(fieldDefs)
    32#set the modelid and the zingg dir
    33args.setModelId("100")
    34args.setZinggDir("./models")
    35args.setNumPartitions(4)
    36args.setLabelDataSampleSize(0.5)
    37
    38# Set the blocking strategy for the Zingg Model as either DEFAULT or WIDER - if you do not set anything, the model follows DEFAULT strategy
    39args.setBlockingModel("DEFAULT")
    40
    41#setting pass thru condition
    42args.setPassthroughExpr("fname = 'matilda'")
    43
    44#setting deterministic matching conditions
    45dm1 = DeterministicMatching('fname','stNo','add1')
    46dm2 = DeterministicMatching('ssn')
    47dm3 = DeterministicMatching('fname','stNo','lname')
    48args.setDeterministicMatchingCondition(dm1,dm2,dm3)
    49
    50#reading dataset into inputPipe and setting it up in 'args'
    51#below line should not be required if you are reading from in memory dataset
    52#in that case, replace df with input df
    53schema = "recId string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string"
    54inputPipe = ECsvPipe("testFebrl", "examples/febrl/test.csv", schema)
    55args.setData(inputPipe)
    56
    57outputPipe = ECsvPipe("resultFebrl", "/tmp/febrlOutput")
    58outputPipe.setHeader("true")
    59args.setOutput(outputPipe)
    60
    61# Zingg execution for the given phase
    62# options = ClientOptions([ClientOptions.PHASE,"findAndLabel"])
    63
    64options = ClientOptions([ClientOptions.PHASE,"trainMatch"])
    65zingg = EZingg(args, options)
    66zingg.initAndExecute()
    67
    68incrArgs = IncrementalArguments()
    69incrArgs.setParentArgs(args)
    70incrPipe = ECsvPipe("testFebrlIncr", "examples/febrl/test-incr.csv", schema)
    71incrArgs.setIncrementalData(incrPipe)
    72
    73incrOptions = ClientOptions([ClientOptions.PHASE,"runIncremental"])
    74zinggIncr = EZingg(incrArgs, incrOptions)
    75zinggIncr.initAndExecute()
:::
:::
:::
:::
:::

::: {.rst-footer-buttons role="navigation" aria-label="Footer"}
[Next []{.fa .fa-arrow-circle-right
aria-hidden="true"}](zinggEC.html "Zingg Enterpise Entity Resolution Package"){.btn
.btn-neutral .float-right accesskey="n" rel="next"}
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
