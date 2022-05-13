<p>
    <table class="table table-borderless" style="width:auto" >
    <thead class="thead thead-dark">
    </thead>
<tbody>
    <tr>
        <th>StopWord</th>
        <th>Count</th>
    </tr>
    <#list stopWords as words>
    <tr>
        <td>
        ${words[0]!}
        </td>
        <td>
        ${words[1]?string["0"]!}
        </td>
    </tr>
    </#list>
</tbody>
</table>
</p>
