<html>
<head>
  <title>Zingg model documentation</title>
</head>
<body>
  <h1>Model ${modelId}</h1>
  <p>Field Definitions</p>
  <p>Datasets</p>
  <p>

  |<#list clusters as c> Cluster ${c?counter} <#sep>|</#list>|
|<#list clusters as c>--- <#sep>|</#list>|
<#list 0 ..< maxEntities as entityIndex>
|<#list clusters as c> ${c[entityIndex]!} <#sep>|</#list>|
</#list>

  </p>
</body>
</html>