package zingg;


public class TestZingg {
	
	/*
	
	private transient JavaSparkContext sc;
	private String matchFile = "/tmp/matchFile.txt";
	private Zingg zingg;
	private Arguments args;
	private FieldDefinitions fieldDefs;
	
	@Before
    public void setUp() throws Exception, ZinggClientException{
      sc = new JavaSparkContext("local", "JavaAPISuite");
      
      FieldDefinition one = new FieldDefinition(0, MatchType.FUZZY, DataType.STRING);
	  FieldDefinition two = new FieldDefinition(1, MatchType.FUZZY, DataType.STRING);
	  List<FieldDefinition> fieldDef = new ArrayList<FieldDefinition>();
	  fieldDef.add(one);
	  fieldDef.add(two);
	  fieldDefs = new FieldDefinitions(fieldDef, 2);
		
	  args = new Arguments();
	  args.setDelimiter(",");
	  args.setFieldDefinition(fieldDef);
	  
	  FileWriter fw = new FileWriter(matchFile);
	  String strs[] = { "A,C", "B,C", "D,E" };

	  for (int i = 0; i < strs.length; i++) {
	      fw.write(strs[i] + "\n");
	  }
	  fw.close();
	  	
	  args.setMatchDataFile(matchFile);
	  zingg = new Zingg(args,sc, System.getProperty("license"));
    }

    @After
    public void tearDown() {
      sc.stop();
      sc = null;
      // To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown
      System.clearProperty("spark.driver.port");
      File file = new File(matchFile);
      file.delete();
      File file1 = new File("/tmp/zingg*");
      file1.delete();
    }


	@Test
	public void testIsValidPairValid() throws ZinggClientException {
		Zingg zingg = new Zingg();
		Tuple first = Tuple.size(1);
		first.id = 1l;
		Tuple second = Tuple.size(1);
		second.id = 2l;
		Tuple2<Tuple, Tuple> pair = mock(Tuple2.class);
		when(pair._1()).thenReturn(first);
		when(pair._2()).thenReturn(second);
		assertTrue(zingg.isValidPair(pair));
	}
	
	@Test
	public void testIsValidPairInValidEquals() throws ZinggClientException {
		Zingg zingg = new Zingg();
		Tuple first = Tuple.size(1);
		first.id = 1l;
		Tuple second = Tuple.size(1);
		second.id = 1l;
		Tuple2<Tuple, Tuple> pair = mock(Tuple2.class);
		when(pair._1()).thenReturn(first);
		when(pair._2()).thenReturn(second);
		assertFalse(zingg.isValidPair(pair));
	}

	@Test
	public void testIsValidPairInvalid() throws ZinggClientException {
		Zingg zingg = new Zingg();
		Tuple first = Tuple.size(1);
		first.id = 2l;
		Tuple second = Tuple.size(1);
		second.id = 1l;
		Tuple2<Tuple, Tuple> pair = mock(Tuple2.class);
		when(pair._1()).thenReturn(first);
		when(pair._2()).thenReturn(second);
		assertFalse(zingg.isValidPair(pair));
	}
	
	
	private Tuple getTuple(String a) {
		Tuple on = new Tuple(a.split(","));
		on.set(fieldDefs.getTypes(), fieldDefs.getCoercibleTypes(), fieldDefs.getPreprocessor(),
				",");
		return on;
	}
	
	@Test
	public void testGetTrainingPair() throws Exception, ZinggClientException{
		List<String> strings = Arrays.asList("TP1,A,B", 
				"TP1,A,B1", "TP2,A,C", 
				"TP2,A,C1", "TP3,B,D","TP1,A1,B");
		Tuple one = getTuple("A,B"); 
		one.id = new Long(0);
		Tuple two = getTuple("A,B1");
		two.id = new Long(1);
		Tuple three = getTuple("A,C");
		three.id = new Long(2);
		Tuple four = getTuple("A,C1");
		four.id = new Long(3);
		Tuple five = getTuple("A1,B");
		five.id = new Long(5);
		
		JavaRDD<String> lines = sc.parallelize(strings);
		List<Tuple2<Tuple,Tuple>> pairs = zingg.getTrainingPair(lines).collect();
		Tuple2<Tuple,Tuple> valid1 = new Tuple2<Tuple,Tuple>(one, two);
		Tuple2<Tuple,Tuple> valid2 = new Tuple2<Tuple,Tuple>(three, four);
		Tuple2<Tuple,Tuple> valid3 = new Tuple2 <Tuple,Tuple>(one, five);
		Tuple2<Tuple,Tuple> valid4 = new Tuple2<Tuple,Tuple>(two, five);
		for (Tuple2 t: pairs) {
			System.out.println("Tuple pair found " + t);
		}
		assertEquals(4, pairs.size());
		assertThat(pairs, containsInAnyOrder(valid1, valid2, valid3, valid4));
		assertThat(pairs, not(contains(new Tuple2(two,one))));
	}
	*/
	

}
