from setuptools import *

setup(
    name='zingg',  
    version='0.0.2',
    include_package_data=True,
    # packages=['zingg_jar'],
    scripts=['zingg.py'] ,
    author="Sonal Goyal",
    author_email="sonalgoyal4@gmail.com",
    description="Zingg.ai environment package",
    long_description=open("README.md").read(),
    long_description_content_type="text/markdown",
    # url="git_repo_link",
    packages=find_packages(),
    # py_modules=['module1', ..],
    # install_requires=[
    #    'requests', 'click', 'configparser'
    # ],
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: GNU Affero General Public License v3",
        "Operating System :: OS Independent",
    ],
    zip_safe = False
)
