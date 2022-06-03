from setuptools import *

setup(
    name='zingg',  
    version='0.0.2',
    include_package_data=True,
    scripts=['zingg.py'] ,
    author="Zingg.AI",
    author_email="sonalgoyal4@gmail.com",
    description="Zingg.ai environment package",
    long_description=open("README.md").read(),
    long_description_content_type="text/markdown",
    # url="git_repo_link",
    packages=find_packages(),
    # packages=['zingg_jar', .. ],
    # py_modules=['module1', ..],
    # install_requires=[
    #    'python 3.6+', 'spark 3.1.2', 'configparser', ..
    # ],
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: GNU Affero General Public License v3",
        "Operating System :: OS Independent",
    ],
    zip_safe = False
)
