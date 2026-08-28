#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  Jêbirina Ewle / Güvenli Silme - macOS surumunu uretir
#
#  ONEMLI: jpackage capraz derleme YAPAMAZ. .dmg dosyasi ancak bir Mac
#  uzerinde uretilebilir. Bu betigi projeyi kopyaladiktan sonra Mac'te calistir.
#
#  Gereken: JDK 17+ (ornegin `brew install --cask temurin17`)
# ---------------------------------------------------------------------------
set -e
cd "$(dirname "$0")"

echo "==> Java surumu:"
java -version

echo "==> macOS paketi olusturuluyor..."
./gradlew :desktop:packageDmg

echo
echo "==> Hazir:"
find desktop/build/compose/binaries/main/dmg -name "*.dmg"
echo
echo "Not: Uygulama imzasiz oldugu icin macOS ilk aciliste engelleyebilir."
echo "     Sag tik > Ac  (veya Sistem Ayarlari > Gizlilik ve Guvenlik > Yine de Ac)"
echo
echo "Sadece calistirmak icin (paketlemeden):  ./gradlew :desktop:run"
