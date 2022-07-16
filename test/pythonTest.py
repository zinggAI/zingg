"""
import glob
import unittest

testSuite = unittest.TestSuite()
test_file_strings = glob.glob('test_*.py')
module_strings = [str[0:len(str)-3] for str in test_file_strings]
[__import__(str) for str in module_strings]
suites = [unittest.TestLoader().loadTestsFromName(str) for str in module_strings]
[testSuite.addTest(suite) for suite in suites]
print(testSuite)

result = unittest.TestResult()
testSuite.run(result)
print(result)

#Ok, at this point I have a result
#How do I display it as the normal unit test command line output?
if __name__ == "__main__":
    unittest.main()
"""
import unittest

if __name__ == '__main__':
    testsuite = unittest.TestLoader().discover('.')
    unittest.TextTestRunner(verbosity=1).run(testsuite)
    