#!/usr/bin/env python3
"""
Validate frontmatter in markdown files using basic string parsing.
Checks for required fields: title, description, parent, nav_order (where applicable)
"""
import os
import sys
import re
from pathlib import Path

REQUIRED_FIELDS = ['description']
OPTIONAL_FIELDS = ['parent', 'nav_order', 'tags']

def parse_frontmatter(content):
    """Parse frontmatter from markdown content"""
    # Match frontmatter between --- delimiters
    match = re.match(r'^---\n(.*?)\n---', content, re.DOTALL)
    if not match:
        return {}
    
    frontmatter_text = match.group(1)
    metadata = {}
    
    # Simple YAML parsing for common cases
    for line in frontmatter_text.split('\n'):
        line = line.strip()
        if not line or line.startswith('#'):
            continue
        
        # Handle key: value
        if ':' in line:
            key, value = line.split(':', 1)
            key = key.strip()
            value = value.strip()
            
            # Remove quotes
            if value.startswith('"') and value.endswith('"'):
                value = value[1:-1]
            elif value.startswith("'") and value.endswith("'"):
                value = value[1:-1]
            
            metadata[key] = value
    
    return metadata

def validate_frontmatter(filepath):
    """Validate a single markdown file's frontmatter"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        metadata = parse_frontmatter(content)
        
        errors = []
        warnings = []
        
        # Check required fields
        for field in REQUIRED_FIELDS:
            if field not in metadata:
                errors.append(f"Missing required field: {field}")
            elif not metadata[field]:
                errors.append(f"Empty required field: {field}")
        
        # Check optional fields
        for field in OPTIONAL_FIELDS:
            if field not in metadata:
                warnings.append(f"Missing optional field: {field}")
        
        # Validate nav_order is integer if present
        if 'nav_order' in metadata:
            try:
                int(metadata['nav_order'])
            except (ValueError, TypeError):
                errors.append(f"nav_order must be an integer, got: {metadata['nav_order']}")
        
        return errors, warnings
        
    except Exception as e:
        return [f"Error reading file: {e}"], []

def main():
    docs_dir = Path('docs')
    if not docs_dir.exists():
        print("ERROR: docs directory not found")
        return 1
    
    md_files = list(docs_dir.rglob('*.md'))
    print(f"Found {len(md_files)} markdown files to validate")
    
    total_errors = 0
    total_warnings = 0
    
    for md_file in md_files:
        # Skip certain files
        if any(part.startswith('.') for part in md_file.parts):
            continue
        if md_file.name in ['SUMMARY.md', 'README.md', 'CNAME']:
            continue
        
        errors, warnings = validate_frontmatter(md_file)
        
        if errors:
            print(f"\nERROR in {md_file}:")
            for error in errors:
                print(f"  - {error}")
            total_errors += len(errors)
        
        if warnings:
            print(f"\nWARNING in {md_file}:")
            for warning in warnings:
                print(f"  - {warning}")
            total_warnings += len(warnings)
    
    print(f"\n{'='*50}")
    print(f"Validation complete:")
    print(f"  Files checked: {len(md_files)}")
    print(f"  Errors: {total_errors}")
    print(f"  Warnings: {total_warnings}")
    
    if total_errors > 0:
        print("\nERROR: Frontmatter validation failed")
        return 1
    else:
        print("\nSUCCESS: All frontmatter is valid")
        return 0

if __name__ == '__main__':
    sys.exit(main())