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
  <a href="../model.html">
  <div class="justify-content-end">Model ${modelId}</div>
  </a>
</nav>
   <p>
  <table class="table table-borderless">
  <thead class="thead thead-dark">
  </thead>
  <tbody>
  <tr>
    <th>Word</th>
    <th>Count</th>
  </tr>

  <#list stopWords as words>
  <tr>
    <td>
      ${words[0]!}
      </td>
      <td>
      ${words[1]!}
      </td>
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