exit_on_error() {
    echo "$1"
    exit 1
}

fuzzy_patch_task() {
  local target=$1$2

  ./gradlew apply"$target"Patches || {
    echo "Something went wrong when applying $target patches"
    ./gradlew apply"$target"PatchesFuzzy || exit_on_error "An error occurred when applying $target patches!"
  }
  ./gradlew rebuild"$target"Patches || exit_on_error "An error occurred when rebuilding $target file patches!"

  ./gradlew apply$1Patches || exit_on_error "An error occurred when applying $1 patches!"
  ./gradlew rebuild$1Patches || exit_on_error "An error occurred when rebuilding $1 patches!"
}

oldHash=$(grep "paperRef=" gradle.properties | cut -d "=" -f2)
newHash=$(curl -s https://api.github.com/repos/PaperMC/paper/commits/$1 | jq -r .sha)

if [ "$oldHash" = "$newHash" ]; then
    echo "Upstream has not updated!"
    exit 0
fi

echo "Updating paper: $oldHash -> $newHash"

sed -i '' "s/$oldHash/$newHash/g" gradle.properties
git add gradle.properties

fuzzy_patch_task "PaperApi" "File"
fuzzy_patch_task "Server" "File"
fuzzy_patch_task "Minecraft" "Source"

./gradlew applyAllPatches || exit_on_error "An error occurred when merging patches!"
./gradlew rebuildPaperApiPatches || exit_on_error "An error occurred when rebuilding api patches!"
./gradlew rebuildAllServerPatches || exit_on_error "An error occurred when rebuilding server patches!"
./gradlew createPaperclipJar || exit_on_error "An error occurred when building!"

scripts/upstreamCommit.sh $oldHash $newHash

echo "Created new commit, please review before pushing."
