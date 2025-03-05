#!/bin/bash
# This script converts all SVG files in the img/graphs directory into PDF files
# using Inkscape. Ensure you are using Inkscape version 1.x and compile with --shell-escape if needed.

# Directory containing the SVG files
SVG_DIR="img/graphs"

# Loop through each SVG file in the directory
for svg in "$SVG_DIR"/*.svg; do
    # Construct the PDF filename replacing the .svg extension with .pdf
    pdf="${svg%.svg}.pdf"
    echo "Converting $svg to $pdf"
    # Convert the SVG file to PDF using Inkscape
    inkscape "$svg" --export-type=pdf --export-filename="$pdf"
done

echo "Conversion complete."