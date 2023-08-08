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
#  Zingg
#  Copyright (C) 2021-Present  Zingg Labs,inc
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Affero General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Affero General Public License for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

import unittest

if __name__ == '__main__':
    testsuite = unittest.TestLoader().discover('.')
    unittest.TextTestRunner(verbosity=1).run(testsuite)
    