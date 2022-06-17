##this file is based on pyspark packaging - we have customized it for zingg
 
import glob
import os
import sys
from setuptools import *
from shutil import copyfile, copytree, rmtree

TEMP_PATH = "deps"
ZINGG_HOME = os.path.abspath("../../")


print("zingg home is " , ZINGG_HOME)

# Provide guidance about how to use setup.py
incorrect_invocation_message = """
If you are installing zingg from zingg source, you must first build Zingg and
run sdist.

    To build zingg with maven you can run:
     $ZINGG_HOME/mvn -DskipTests clean package
    Building the source dist is done in the Python directory:
      cd python
      python setup.py sdist
      pip install dist/*.tar.gz"""


# Figure out where the jars are we need to package with zingg.
JARS_PATH = glob.glob(os.path.join(ZINGG_HOME, "assembly/target/zingg*.jar"))

if len(JARS_PATH) == 1:
    JARS_PATH = JARS_PATH[0]

print("jar path is " , JARS_PATH)

EXAMPLES_PATH = os.path.join(ZINGG_HOME, "python/examples")
SCRIPTS_PATH = os.path.join(ZINGG_HOME, "scripts")
DATA_PATH = os.path.join(ZINGG_HOME, "examples")
LICENSES_PATH = os.path.join(ZINGG_HOME, "licenses")

SCRIPTS_TARGET = os.path.join(TEMP_PATH, "scripts")
JARS_TARGET = os.path.join(TEMP_PATH, "jars")
EXAMPLES_TARGET = os.path.join(TEMP_PATH, "examples")
DATA_TARGET = EXAMPLES_TARGET #os.path.join(TEMP_PATH, "examples")
LICENSES_TARGET = os.path.join(TEMP_PATH, "licenses")

# Check and see if we are under the spark path in which case we need to build the symlink farm.
# This is important because we only want to build the symlink farm while under Spark otherwise we
# want to use the symlink farm. And if the symlink farm exists under while under Spark (e.g. a
# partially built sdist) we should error and have the user sort it out.
in_spark = (os.path.isfile("../../core/src/main/java/zingg/Trainer.java") == 1)


def _supports_symlinks():
    """Check if the system supports symlinks (e.g. *nix) or not."""
    return getattr(os, "symlink", None) is not None

if (in_spark):
    # Construct links for setup
    try:
        os.mkdir(TEMP_PATH)
    except:
        print("Temp path for symlink to parent already exists {0}".format(TEMP_PATH),
              file=sys.stderr)
        os.rmdir(TEMP_PATH)
        sys.exit(-1)


try:
    
    if (in_spark):
        # Construct the symlink farm - this is necessary since we can't refer to the path above the
        # package root and we need to copy the jars and scripts which are up above the python root.
        if _supports_symlinks():
            os.symlink(JARS_PATH, JARS_TARGET)
            os.symlink(SCRIPTS_PATH, SCRIPTS_TARGET)
            os.symlink(EXAMPLES_PATH, EXAMPLES_TARGET)
            #os.symlink(DATA_PATH, DATA_TARGET)
            os.symlink(LICENSES_PATH, LICENSES_TARGET)
        else:
            # For windows fall back to the slower copytree
            copytree(JARS_PATH, JARS_TARGET)
            copytree(SCRIPTS_PATH, SCRIPTS_TARGET)
            copytree(EXAMPLES_PATH, EXAMPLES_TARGET)
            copytree(DATA_PATH, DATA_TARGET)
            copytree(LICENSES_PATH, LICENSES_TARGET)
         
    else:
        # If we are not inside of SPARK_HOME verify we have the required symlink farm
        if not os.path.exists(JARS_TARGET):
            print("To build packaging must be in the python directory under the SPARK_HOME.",
                  file=sys.stderr)

    if not os.path.isdir(SCRIPTS_TARGET):
        print(incorrect_invocation_message, file=sys.stderr)
        sys.exit(-1)

    # Scripts directive requires a list of each script path and does not take wild cards.
    script_names = os.listdir(SCRIPTS_TARGET)
    scripts = list(map(lambda script: os.path.join(SCRIPTS_TARGET, script), script_names))
    # We add find_spark_home.py to the bin directory we install so that pip installed PySpark
    # will search for SPARK_HOME with Python.
    #scripts.append("pyspark/find_spark_home.py")

    with open('README.md') as f:
        long_description = f.read()

    setup(
    name='zingg',  
    version='0.3.4',
    include_package_data=True,
    author="Zingg.AI",
    author_email="sonalgoyal4@gmail.com",
    description="Zingg Entity Resolution, Data Mastering and Deduplication",
    long_description=open("README.md").read(),
    long_description_content_type="text/markdown",
    url="https://github.com/zinggAI/zingg",
    #packages=find_packages(),
    packages=['zingg','zingg.pipes'],
    package_dir={
            'zingg.jars': 'deps/jars',
            'zingg.bin': 'deps/bin',
            'zingg.licenses': 'deps/licenses',
            'zingg.examples': 'deps/examples',
        },
        package_data={
            'zingg.jars': ['*.jar'],
            'zingg.bin': ['*'],
            'zingg.python.lib': ['*.zip'],
            'zingg.licenses': ['*.txt'],
            'zingg.examples': ['*.py', '*/examples/*.py']},
        scripts=scripts,
        license='http://www.apache.org/licenses/LICENSE-2.0',
        install_requires=['py4j==0.10.9'],
        extras_require={
            'zingg': ['pyspark>=3.1.2']
        },
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: GNU Affero General Public License v3",
        "Operating System :: OS Independent",
    ],
    zip_safe = False, 
    keywords="Entity Resolution, deduplication, record linkage, data mastering, identity resolution"
)
finally:
    os.rmdir(TEMP_PATH)
    #print ("do nothing")


