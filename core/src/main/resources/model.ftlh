<html>
<head>
  <title>Zingg model documentation</title>
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">
</head>
<body>
<nav class="navbar navbar-light bg-light">
  <a class="navbar-brand" href="https://www.zingg.ai">
    <img src="https://github.com/zinggai/zingg/raw/main/assets/zinggWhiteTransparent.png" class="d-inline-block align-top" alt="">
  </a>
  <div class="justify-content-end">Model ${modelId}</div>
</nav>
   <p>
  <table class="table table-borderless">

  
  <thead class="thead thead-dark">
  <tr>
    <th>Cluster</th>
    <#list 3 ..< numColumns -1 as entityIndex>
    <th> ${columns[entityIndex]!}</th>
    </#list>
  </tr>
  </thead>
  <tbody>
  <#list clusters as c>
  <tr>
    <#if c?is_odd_item>
      <td rowspan="2"
      <#if c[numColumns-1] == 0>  class="table-danger"
      </#if>
      <#if c[numColumns -1] == 1> class="table-success"
      </#if>
      >
      ${c[0]!}
      </td>
    </#if>
  
  
    <#list 3 ..< numColumns -1 as entityIndex>
    <td 
     <#if c?is_even_item>
      <#if c[numColumns-1] == 0>  class="border-bottom bottom-danger"
      </#if>
      <#if c[numColumns -1] == 1> class="border-bottom border-success"
      </#if>
     </#if>
     >
      ${c[entityIndex]!}
     </td>
    </#list>
  </tr>
  
  </#list>
 </tbody>
  </table>
   

</p>
</body>
</html>
<style>
    .header{
        position:sticky;
        top: 0 ;
    }
</style>