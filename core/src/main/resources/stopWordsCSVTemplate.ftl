StopWord, Count
<#list stopWords as word>
${word[0]}, ${word[1]?string["0"]}
</#list>