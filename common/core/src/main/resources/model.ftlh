<html>
<head>
  <title>Zingg Model Documentation</title>
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">
</head>
<body>
<nav class="navbar navbar-light bg-light">
  <a class="navbar-brand" href="https://www.zingg.ai" style="float:left;" >
    <img src="https://github.com/zinggai/zingg/raw/main/assets/zinggWhiteTransparent.png" class="d-inline-block align-top" alt="">
  </a>
  <div class="justify-content=flex-end" style="float:right;" >Model ${modelId}</div>
 </nav>
  <p>
  <table class="table table-borderless">
	  <tbody>
		  	<tr>
				<th class="border-right border-white" style="width: 160px;" >Unmarked ${unmarkedPairs}/${identifiedPairs}, Marked ${markedPairs}/${identifiedPairs} (<font color="#28a745">${matchPairs} Matches</font>, <font color="#FF4500">${nonMatchPairs} Non-Matches</font>, ${notSurePairs} Unsure)</th>
		  	</tr>
	  </tbody>
  </table>
 <p>
  <table class="table table-hover">
  <table class="table table-striped"> 

  <div class="container-fluid">
    <thead class="thead thead-light">
      <tr>
        <th class="border-right border-white" style="width: 160px;" >CLUSTER</th>
        <div class="clearfix visible-xs-block"></div>
        <#list 3 ..< numColumns as entityIndex>
        <th class="border-right border-white" > <a href="${columns[entityIndex]}.html"> ${columns[entityIndex]!?upper_case} </a></th>
        </#list>
      </tr>
    </thead>
  </div>  

  <tbody>

  <#list clusters as c>
  <tr>
    <#if c?is_odd_item>
      <td rowspan="2"
      <#if c[isMatchColumnIndex] == 0>  class="table-danger" style="border-bottom:1px solid #FF4500; border-right: 1px solid #FF4500 ;"
      </#if>
      <#if c[isMatchColumnIndex] == 1> class="table-success" style="border-bottom:1px solid #28a745; border-right: 1px solid #28a745 ;"
      </#if>
      >
      ${c[clusterColumnIndex]!}
      </td>
    </#if>
  
  
    <#list 3 ..< numColumns as entityIndex>
    <td 
     <#if c?is_even_item>
      <#if c[isMatchColumnIndex] == 0>  class="border-bottom border-danger" style="border-right: 1px solid #FF4500  ;"
      </#if>
      <#if c[isMatchColumnIndex] == 1> class="border-bottom border-success" style="border-right: 1px solid #28a745  ;"
      </#if>
     <#else>
      <#if c[isMatchColumnIndex] == 0>  style="border-right: 1px solid #FF4500  ;"
      </#if>
      <#if c[isMatchColumnIndex] == 1>  style="border-right: 1px solid #28a745  ;"
      </#if>
     </#if>
    > 
      <#if columns[entityIndex]! == "z_score" && c?is_even_item >
      <#elseif columns[entityIndex]! == "z_isMatch" && c?is_even_item >
      <#else>${c[entityIndex]!}
      </#if>

      
    </td>
    </#list>
  </tr>
  
  </#list>
 </tbody>
  </table>
  </table>
   

</p>
</body>
</html>
<style>
    .header{
        position:relative;
        top: 0 ;
    }.table{
      table-layout: relative;
      width: 100%;
    }
</style>