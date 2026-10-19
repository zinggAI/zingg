"""Structural checks for SPDX 2.3 JSON and relationship integrity."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any


def validate(document: Any) -> None:
    if not isinstance(document, dict) or document.get("spdxVersion") != "SPDX-2.3":
        raise ValueError("SBOM must be an SPDX-2.3 document object")
    if document.get("dataLicense") != "CC0-1.0":
        raise ValueError("SPDX dataLicense must be CC0-1.0")
    if document.get("SPDXID") != "SPDXRef-DOCUMENT":
        raise ValueError("SPDX document identifier is invalid")
    if not isinstance(document.get("documentNamespace"), str) or not document["documentNamespace"].startswith("https://"):
        raise ValueError("SPDX documentNamespace is missing or invalid")
    info = document.get("creationInfo")
    if not isinstance(info, dict) or not info.get("creators") or not info.get("created"):
        raise ValueError("SPDX creationInfo is incomplete")
    packages = document.get("packages")
    if not isinstance(packages, list) or not packages:
        raise ValueError("SPDX packages must be a non-empty list")
    ids = {"SPDXRef-DOCUMENT"}
    package_ids = set()
    for package in packages:
        if not isinstance(package, dict):
            raise ValueError("SPDX package entry must be an object")
        package_id = package.get("SPDXID")
        if not isinstance(package_id, str) or not re.fullmatch(r"SPDXRef-[A-Za-z0-9.-]+", package_id):
            raise ValueError(f"invalid SPDX package identifier: {package_id!r}")
        if package_id in ids:
            raise ValueError(f"duplicate SPDX identifier: {package_id}")
        ids.add(package_id)
        package_ids.add(package_id)
        for key in ("name", "versionInfo", "downloadLocation", "licenseConcluded", "licenseDeclared", "copyrightText"):
            if not isinstance(package.get(key), str) or not package[key]:
                raise ValueError(f"SPDX package {package_id} is missing {key}")
        if package.get("filesAnalyzed") is not False:
            raise ValueError(f"SPDX package {package_id} must explicitly state filesAnalyzed=false")
        if (package["name"], package["versionInfo"]) != ("zingg-duckdb", "0.1.0"):
            references = package.get("externalRefs")
            purls = [reference.get("referenceLocator") for reference in references
                     if isinstance(reference, dict)
                     and reference.get("referenceCategory") == "PACKAGE-MANAGER"
                     and reference.get("referenceType") == "purl"] if isinstance(references, list) else []
            if len(purls) != 1:
                raise ValueError(f"SPDX dependency {package_id} must declare exactly one package URL")
            if not isinstance(purls[0], str) or not re.fullmatch(
                    r"pkg:[A-Za-z0-9.+-]+/[^\s@]+@[^\s?]+(?:\?[^\s]*)?", purls[0]):
                raise ValueError(f"SPDX dependency {package_id} has an invalid package URL")
    relationships = document.get("relationships")
    if not isinstance(relationships, list) or not relationships:
        raise ValueError("SPDX relationships must be a non-empty list")
    for relationship in relationships:
        if not isinstance(relationship, dict):
            raise ValueError("SPDX relationship must be an object")
        source = relationship.get("spdxElementId")
        target = relationship.get("relatedSpdxElement")
        if source not in ids or target not in ids:
            raise ValueError(f"SPDX relationship has dangling endpoint: {source!r} -> {target!r}")
        if not isinstance(relationship.get("relationshipType"), str) or not relationship["relationshipType"]:
            raise ValueError("SPDX relationshipType is required")
    if not any(r.get("spdxElementId") == "SPDXRef-DOCUMENT" and r.get("relationshipType") == "DESCRIBES"
               and r.get("relatedSpdxElement") in package_ids for r in relationships):
        raise ValueError("SPDX document must DESCRIBE at least one package")


def main(argv: list[str]) -> int:
    if len(argv) != 2:
        print("usage: verify_spdx.py <sbom.spdx.json>", file=sys.stderr)
        return 2
    path = Path(argv[1])
    try:
        validate(json.loads(path.read_text(encoding="utf-8-sig")))
    except (OSError, json.JSONDecodeError, ValueError) as error:
        print(f"SPDX verification failed: {error}", file=sys.stderr)
        return 1
    print("SPDX_VERIFY_SUCCESS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
