# ==========================================
# 1. KATMAN: BUILD (DERLEME) KATMANI
# ==========================================
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Önce sadece pom.xml'i kopyalıyoruz. Bu, Docker'ın bağımlılıkları cache'lemesini sağlar.
# Her kod değişiminde baştan kütüphane indirmesini engeller, hızı katlar.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Şimdi tüm kaynak kodları kopyalıyoruz ve testleri atlayarak projeyi derliyoruz
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# 2. KATMAN: RUNTIME (ÇALIŞTIRMA) KATMANI
# ==========================================
# En hafif Java sürümü olan Alpine imajını kullanıyoruz ki RAM'i şişirmesin.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Build katmanında üretilen saf .jar dosyasını alıp bu katmana kopyalıyoruz
# Böylece Maven, kaynak kodlar vs. geride bırakılıyor, katmanlar izole ediliyor.
COPY --from=build /app/target/*.jar app.jar

# Hiçbir ayar hardcoded değil! application.properties içindeki değişkenler
# dışarıdan (Environment Variables üzerinden) okunarak sistem ayağa kaldırılacak.
ENTRYPOINT ["java", "-jar", "app.jar"]