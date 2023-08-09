#!/bin/bash

# Set TARGET_DIRECTORY to the current directory and its subdirectories
TARGET_DIRECTORY='.'
EXCLUDE_FILES=("conf.py" "version.py" "__init__.py")

for i in $(find "$TARGET_DIRECTORY" -type f \( -name "*.java" -o -name "*.py" \))
do
  # Check if the file should be excluded
  if [[ " ${EXCLUDE_FILES[@]} " =~ " ${i##*/} " ]]; then
    continue
  fi
  
  if ! grep -q Copyright "$i"
  then
    # Check file extension and set comment style and copyright header
    if [[ "$i" == *.java ]]; then
        comment_start='/*'
        comment_end='*/'
        copyright_header=$(sed -e 's/^/ \* /' copyright.txt)
    elif [[ "$i" == *.py ]]; then
        comment_start='#'
        comment_end=''
        copyright_header=$(sed -e 's/^/# /' copyright.txt)
    fi
    
    # Read the contents of the file
    file_content=$(cat "$i")
    
    # Create a temporary file to store the modified content
    temp_file=$(mktemp)
    
    # Append license header with comment style
    echo "$comment_start" > "$temp_file"
    echo "$copyright_header" >> "$temp_file"
    echo "$comment_end" >> "$temp_file"
    echo >> "$temp_file"
    echo "$file_content" >> "$temp_file"
    
    # Replace the original file with the modified content
    mv "$temp_file" "$i"
    
    echo "Added license to: $i"
  fi
done

echo "License headers added to .java and .py files in $TARGET_DIRECTORY and its subdirectories."
