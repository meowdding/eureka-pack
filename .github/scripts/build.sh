mkdir -p ./build
rm -rf ./build/eureka.cats
rm -rf ./build/pack.mcmeta

cat > build/pack.mcmeta << EOL
{
  "pack": {
    "description": {
      "text": "",
      "extra": [{ "text": "Eureka", "color": "gray" }]
    },
    "min_format": 69,
    "max_format": 255
  },
}
EOL

  echo "Building version..."
  ./.github/scripts/catsquash build/eureka.cats ./eureka