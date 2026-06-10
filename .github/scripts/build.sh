mkdir -p ./build
rm -rf ./build/pack.cats
rm -rf ./build/pack.mcmeta

cat > build/pack.mcmeta << EOL
{
  "pack": {
    "description": {
      "text": "Use Catharsis-1.0.0-beta.17 or higher!",
      "color": "red",
      "bold": true
    },
    "min_format": 69,
    "max_format": 255
  }
}
EOL

  echo "Building version..."
  ./.github/scripts/catsquash build/pack.cats ./eureka