# Deterministic Matching

Zingg enterprise offers a new feature of deterministic matching along side already available probabilistic matching. In the entity resolution space, we refer to exact matching on attributes as deterministic matching. When used together this allows cases where user has given some sort of unique id in some rows to match accurately together due to deterministic matching and they will than match with others rows where id is missing via probabilistic matching.

Example for configuring it in json:

"deterministicMatching":[
	{
		"matchCondition":[
			{
				"fieldName":"fname"
			},
			{
				"fieldName":"stNo"
			},
			{
				"fieldName":"add1"
			}
		]
	}
],


Python code example:

dm1 = DeterministicMatching('fname','stNo','add1')
dm2 = DeterministicMatching('fname','dob','ssn')
dm3 = DeterministicMatching('fname','stNo','lname')
args.setDeterministicMatchingCondition(dm1,dm2,dm3)
