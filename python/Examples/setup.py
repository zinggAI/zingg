##this file is based on pyspark packaging - we have customized it for zingg

import importlib.util
import glob
import os
import sys
from setuptools import *
from shutil import copyfile, copytree, rmtree
from setuptools.command.install import install

try:
    exec(open('version.py').read())
except IOError:
    print("Failed to load Zingg version file for packaging. You must be in Zingg's python dir.",
          file=sys.stderr)
    sys.exit(-1)

try:
    spec = importlib.util.spec_from_file_location("install", "install.py")
    install_module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(install_module)
except IOError:
    print("Failed to load the installing module (install.py) which had to be "
          "packaged together.",
          file=sys.stderr)
    sys.exit(-1)

VERSION = __version__  
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
      python setupExamples.py sdist
      pip install dist/*.tar.gz"""


EXAMPLES_PATH = os.path.join(ZINGG_HOME, "examples")
DATA_PATH = os.path.join(ZINGG_HOME, "models")

EXAMPLES_TARGET = os.path.join(TEMP_PATH, "examples")
DATA_TARGET = os.path.join(TEMP_PATH, "models")

# Check and see if we are under the Zingg path in which case we need to build the symlink farm.
# This is important because we only want to build the symlink farm while under Zingg otherwise we
# want to use the symlink farm. And if the symlink farm exists under while under Zingg (e.g. a
# partially built sdist) we should error and have the user sort it out.
in_zingg = (os.path.isfile("../../core/src/main/java/zingg/Trainer.java") == 1)


def _supports_symlinks():
    """Check if the system supports symlinks (e.g. *nix) or not."""
    return getattr(os, "symlink", None) is not None

if (in_zingg):
    # Construct links for setup
    try:
        os.mkdir(TEMP_PATH)
    except:
        print("Temp path for symlink to parent already exists {0}".format(TEMP_PATH),
              file=sys.stderr)
        os.rmdir(TEMP_PATH)
        sys.exit(-1)

class InstallCommand(install):

    def run(self):
        install.run(self)

try:

    try:
        os.makedirs("deps")
    except OSError:
        # Don't worry if the directory already exists.
        pass
    
    if (in_zingg):
        # Construct the symlink farm - this is necessary since we can't refer to the path above the
        # package root and we need to copy the jars and scripts which are up above the python root.
        if _supports_symlinks():
            os.symlink(EXAMPLES_PATH, EXAMPLES_TARGET)
            os.symlink(DATA_PATH, DATA_TARGET)
        else:
            # For windows fall back to the slower copytree
            copytree(EXAMPLES_PATH, EXAMPLES_TARGET)
            copytree(DATA_PATH, DATA_TARGET)
    
    
    packages = []
    packages.append('deps')

    with open('README.md') as f:
        long_description = f.read()

    setup(
    name='zinggExamples',  
    version=VERSION,
    author="Zingg.AI",
    author_email="sonalgoyal4@gmail.com",
    description="Zingg_Examples",
    long_description=open("README.md").read(),
    long_description_content_type="text/markdown",
    url="https://github.com/zinggAI/zingg",
    packages=packages,
    package_dir={
            'zingg.examples': 'deps/examples',
            'zingg.models': 'deps/models'
        },
        package_data={
            'zingg.examples': ['*'],
            'zingg.models': ['*']
            },
        include_package_data=True,
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
    keywords="Entity Resolution, deduplication, record linkage, data mastering, identity resolution",
    cmdclass={
            'install': InstallCommand,
        },
)
finally:
    if (in_zingg):
        # Depending on cleaning up the symlink farm or copied version
        if _supports_symlinks():
            os.remove(os.path.join(TEMP_PATH, "examples"))
            os.remove(os.path.join(TEMP_PATH, "models"))
        else:
            rmtree(os.path.join(TEMP_PATH, "examples"))
            rmtree(os.path.join(TEMP_PATH, "models"))
        rmtree(TEMP_PATH)
    

