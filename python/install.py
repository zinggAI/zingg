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
