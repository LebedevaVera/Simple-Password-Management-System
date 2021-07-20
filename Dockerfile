FROM airhacks/glassfish
COPY ./target/pm.war ${DEPLOYMENT_DIR}
