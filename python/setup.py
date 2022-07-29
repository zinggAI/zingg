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
ZINGG_HOME = os.path.abspath("../")

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
JARS_PATH = glob.glob(os.path.join(ZINGG_HOME, "assembly/target"))

if len(JARS_PATH) == 1:
    JARS_PATH = JARS_PATH[0]

print("jar path is " , JARS_PATH)

EXAMPLES_PATH = os.path.join(ZINGG_HOME, "examples")
SCRIPTS_PATH = os.path.join(ZINGG_HOME, "scripts")
DATA_PATH = os.path.join(ZINGG_HOME, "models")
CONF_PATH = os.path.join(ZINGG_HOME, "config")
PHASES_PATH = os.path.join(ZINGG_HOME, "python/phases")

SCRIPTS_TARGET = os.path.join("zingg", "scripts")
JARS_TARGET = os.path.join("zingg", "jars")
EXAMPLES_TARGET = os.path.join("zingg", "examples")
DATA_TARGET = os.path.join("zingg", "models")
CONF_TARGET = os.path.join("zingg", "config")
PHASES_TARGET = os.path.join("zingg", "phases")

# Check and see if we are under the Zingg path in which case we need to build the symlink farm.
# This is important because we only want to build the symlink farm while under Zingg otherwise we
# want to use the symlink farm. And if the symlink farm exists under while under Zingg (e.g. a
# partially built sdist) we should error and have the user sort it out.
in_zingg = (os.path.isfile("../core/src/main/java/zingg/Trainer.java") == 1)


def _supports_symlinks():
    """Check if the system supports symlinks (e.g. *nix) or not."""
    return getattr(os, "symlink", None) is not None

class InstallCommand(install):

    def run(self):
        install.run(self)

        # Make sure the destination is always clean.
        # zingg_dist = os.path.join(self.install_lib, "zingg", "zingg-distribution")
        # rmtree(zingg_dist, ignore_errors=True)

        # if (1):
        #     # Note that ZINGG_VERSION environment is just a testing purpose.
        #     zingg_version = install_module.checked_versions(
        #         os.environ.get("ZINGG_VERSION", VERSION).lower())

        #     install_module.install_zingg(
        #         dest=zingg_dist,
        #         zingg_version=zingg_version)

try:

    try:
        os.makedirs("zingg")
    except OSError:
        # Don't worry if the directory already exists.
        pass
    
    if (in_zingg):
        # Construct the symlink farm - this is necessary since we can't refer to the path above the
        # package root and we need to copy the jars and scripts which are up above the python root.
        if _supports_symlinks():
            os.symlink(JARS_PATH, JARS_TARGET)
            os.symlink(SCRIPTS_PATH, SCRIPTS_TARGET)
            os.symlink(EXAMPLES_PATH, EXAMPLES_TARGET)
            os.symlink(DATA_PATH, DATA_TARGET)
            os.symlink(CONF_PATH, CONF_TARGET)
            os.symlink(PHASES_PATH, PHASES_TARGET)
        else:
            # For windows fall back to the slower copytree
            copytree(JARS_PATH, JARS_TARGET)
            copytree(SCRIPTS_PATH, SCRIPTS_TARGET)
            copytree(EXAMPLES_PATH, EXAMPLES_TARGET)
            copytree(DATA_PATH, DATA_TARGET)
            copytree(CONF_PATH, CONF_TARGET)
            copytree(PHASES_PATH, PHASES_TARGET)
    else:
        # If we are not inside of ZINGG_HOME verify we have the required symlink farm
        if not os.path.exists(JARS_TARGET):
            print("To build packaging must be in the python directory under the ZINGG_HOME.",
                  file=sys.stderr)

    if not os.path.isdir(SCRIPTS_TARGET):
        print(incorrect_invocation_message, file=sys.stderr)
        sys.exit(-1)

    # Scripts directive requires a list of each script path and does not take wild cards.
    script_names = os.listdir(SCRIPTS_TARGET)
    scripts = list(map(lambda script: os.path.join(SCRIPTS_TARGET, script), script_names))
    
    packages = []
    packages.append('zingg')
    #packages.append('zingg.pipes')

    with open('README.md') as f:
        long_description = f.read()

    setup(
    name='zingg',  
    version=VERSION,
    author="Zingg.AI",
    author_email="sonalgoyal4@gmail.com",
    description="Zingg Entity Resolution, Data Mastering and Deduplication",
    long_description=open("README.md").read(),
    long_description_content_type="text/markdown",
    url="https://github.com/zinggAI/zingg",
    # packages=find_packages(),
    packages=packages,
    package_dir={
            'zingg.jars': 'zingg/jars',
            'zingg.scripts': 'zingg/scripts',
            'zingg.data': 'zingg/models',
            'zingg.examples': 'zingg/examples',
            'zingg.conf': 'zingg/config',
            'zingg.phases': 'zingg/phases'
        },
        package_data={
            'zingg.jars': ['*.jar'],
            'zingg.scripts': ['*'],
            'zingg.data': ['*'],
            'zingg.examples': ['*.py', '*/examples/*.py'],
            'zingg.conf': ['*'],
            'zingg.phases': ['*'],
            '':['*.py'],
            '':['LICENCE']
            },
        include_package_data=True,
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
    keywords="Entity Resolution, deduplication, record linkage, data mastering, identity resolution",
    cmdclass={
            'install': InstallCommand,
        },
)
finally:
    if (in_zingg):
        # Depending on cleaning up the symlink farm or copied version
        if _supports_symlinks():
            os.remove(os.path.join("zingg", "jars"))
            os.remove(os.path.join("zingg", "scripts"))
            os.remove(os.path.join("zingg", "models"))
            os.remove(os.path.join("zingg", "examples"))
            os.remove(os.path.join("zingg", "phases"))
            os.remove(os.path.join("zingg", "config"))
        else:
            rmtree(os.path.join("zingg", "jars"))
            rmtree(os.path.join("zingg", "scripts"))
            rmtree(os.path.join("zingg", "models"))
            rmtree(os.path.join("zingg", "examples"))
            rmtree(os.path.join("zingg", "phases"))
            rmtree(os.path.join("zingg", "config"))
