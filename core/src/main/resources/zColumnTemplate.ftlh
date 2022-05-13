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
  <p>
    <#if title == "z_cluster">
      <p>"z_cluster - identifies a group of records which match or don't match with each other. For each group, z_cluster is unique. Member records of a group share the same z_cluster."</p>
    <#elseif title == "z_zid">
      <p>"z_zid - an internal id given by Zingg to uniquely identify the record."</p>
    <#elseif title == "z_prediction">
      <p>"z_prediction - what Zingg thinks about this group/pair of records - 0 for not a match, 1 for a match."</p>
    <#elseif title == "z_score">
      <p>"z_score - the probability of a pair of records matching. The higher the score, the more likely they are a match."</p>
    <#elseif title == "z_isMatch">
      <p>z_isMatch - this is the label provided by the user.</p>
    <#elseif title == "z_source">
      <p>z_source - the source of data as set in the name property of the data in the Zingg configuration file.</p>
    <#else>
      <p>${title} - this field is internally used by Zingg.</p>
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