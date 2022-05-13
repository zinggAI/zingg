<html>
<head>
  <title>${title!}</title>
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">
</head>

<body>
  <nav class="navbar navbar-light bg-light">
    <a class="navbar-brand" href="https://www.zingg.ai">
      <img src="https://github.com/zinggai/zingg/raw/main/assets/zinggWhiteTransparent.png" class="d-inline-block align-top" alt="">
    </a>
    <h1> Field - ${title!} </h1>
    <a href="../model.html">
    <div class="justify-content-end">Model ${modelId}</div>
    </a>
  </nav>
  <p>
  <table class="table table-borderless" style="width:auto" >
  <thead class="thead thead-dark">
  </thead>
  <tbody>
  </tbody>
  </table>
    <#if baseMessage??>
      <p>${baseMessage}</p>
    </#if>
  </p>
</body>
</html>

<style>
    .header{
        position:sticky;
        top: 0 ;
    }
</style>