import os
import re
import tarfile
import traceback
import urllib.request
from shutil import rmtree

def checked_package_name(zingg_version):
    """
    Check the generated package name.
    """
    return "%s" % (zingg_version)

def checked_versions(zingg_version):
    """
    Check the valid combinations of supported versions in zingg distributions.

    Parameters
    ----------
    zingg_version : str
        zingg version. It should be X.X.X such as '3.0.0' or zingg-3.0.0.
    """
    if re.match("^[0-9]+\\.[0-9]+\\.[0-9]+$", zingg_version):
        zingg_version = "zingg-%s" % zingg_version
    if not zingg_version.startswith("zingg-"):
        raise RuntimeError(
            "zingg version should start with 'zingg-' prefix; however, " "got %s" % zingg_version
        )

    return zingg_version

def install_zingg(dest, zingg_version):
    """
    Installs zingg that corresponds to the given Hadoop version in the current
    library directory.

    Parameters
    ----------
    dest : str
        The location to download and install the zingg.
    zingg_version : str
        zingg version. It should be zingg-X.X.X form.
    """

    package_name = checked_package_name(zingg_version)
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
