	TITLE = ${title};
	MODEL_ID= ${modelId};
    <#list clusters as c>
        ${c}
    </#list>
	NUM_COLUMNS= ${numColumns};
	FIELD_DEFINITION_COUNT= ${fieldDefinitionCount};
	ISMATCH_COLUMN_INDEX = ${isMatchColumnIndex};
	CLUSTER_COLUMN_INDEX= ${clusterColumnIndex};
