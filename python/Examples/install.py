import os
import re
import tarfile
import traceback
import urllib.request
from shutil import rmtree

def checked_package_name(zinggExamples_version):
    """
    Check the generated package name.
    """
    return "%s" % (zinggExamples_version)

def checked_versions(zinggExamples_version):
    """
    Check the valid combinations of supported versions in zinggExamples distributions.

    Parameters
    ----------
    zinggExamples_version : str
        zinggExamples version. It should be X.X.X such as '3.0.0' or zinggExamples-3.0.0.
    """
    if re.match("^[0-9]+\\.[0-9]+\\.[0-9]+$", zinggExamples_version):
        zinggExamples_version = "zinggExamples-%s" % zinggExamples_version
    if not zinggExamples_version.startswith("zinggExamples-"):
        raise RuntimeError(
            "zinggExamples version should start with 'zinggExamples-' prefix; however, " "got %s" % zinggExamples_version
        )

    return zinggExamples_version

def install_zinggExamples(dest, zinggExamples_version):
    """
    Installs zinggExamples that corresponds to the given Hadoop version in the current
    library directory.

    Parameters
    ----------
    dest : str
        The location to download and install the zinggExamples.
    zinggExamples_version : str
        zinggExamples version. It should be zinggExamples-X.X.X form.
    """

    package_name = checked_package_name(zinggExamples_version)
    package_local_path = os.path.join(dest, "%s.tgz" % package_name)
    
    os.makedirs(dest, exist_ok=True)

    tar = None
    
    try:
        
        print("Installing to %s" % dest)
        tar = tarfile.open(package_local_path, "r:gz")
        for member in tar.getmembers():
            if member.name == package_name:
                # Skip the root directory.
                continue
            member.name = os.path.relpath(member.name, package_name + os.path.sep)
            tar.extract(member, dest)
        return
    except Exception:
        print("Failed to open TAR")
        traceback.print_exc()
        rmtree(dest, ignore_errors=True)
    finally:
        if tar is not None:
            tar.close()
        if os.path.exists(package_local_path):
            os.remove(package_local_path)

    raise IOError("Unable to run INSTALL")
