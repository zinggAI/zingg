<html>
<head>
  <title>Zingg model documentation</title>
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">
</head>
<body>
  <h1>Model ${modelId}</h1>
  <p>Field Definitions</p>
  <p>Datasets</p>
  <p>
  <table>

  <th>
    <td>Cluster</td>
    <#list 3 ..< numColumns as entityIndex>
    <td> ${columns[entityIndex]!}</td>
    </#list>
  </th>
  
  <#list clusters as c>
  <tr>
   <td>${c[0]!}</td>
    <#list 3 ..< numColumns as entityIndex>
    <td>
      ${c[entityIndex]!}
     </td>
    </#list>
  </tr>
  </#list>
  </table>
   

</p>
</body>
</html>