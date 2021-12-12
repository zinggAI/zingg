<html>
<head>
  <title>Zingg model documentation</title>
</head>
<body>
  <h1>Model ${modelId}</h1>
  <p>Field Definitions</p>
  <p>Datasets</p>
  <p>


  <#list clusters as c> 
  
  Cluster ${c?counter} 
  
  </#list>
  
  <#list clusters as c>
  <#list 0 ..< numColumns as entityIndex>
  ${c[entityIndex]!}
  </#list>
  </#list>
  
   

</p>
</body>
</html>