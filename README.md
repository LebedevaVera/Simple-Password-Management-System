# Build
mvn clean package && docker build -t de.hsb.app/pm .

# RUN

docker rm -f pm || true && docker run -d -p 8080:8080 -p 4848:4848 --name pm de.hsb.app/pm 