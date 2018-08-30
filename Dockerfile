FROM frolvlad/alpine-oraclejre8:slim

# Arguments supplied by maven dockerfile plugin
ARG JAR_FILE

# Internal args
ARG IMG_SERVICE_DIR=/opt/srv
ARG IMG_SERVICE_LOG_DIR=${IMG_SERVICE_DIR}/log

VOLUME /tmp

# Create app directory
RUN mkdir -p ${IMG_SERVICE_DIR}
WORKDIR ${IMG_SERVICE_DIR}

# Install app dependencies
ADD target/${JAR_FILE} ${IMG_SERVICE_DIR}/${JAR_FILE}
RUN sh -c 'touch ${IMG_SERVICE_DIR}/${JAR_FILE}'

# Create log dir
RUN mkdir -p ${IMG_SERVICE_LOG_DIR}
VOLUME ${IMG_SERVICE_LOG_DIR}

# Expose server ports
EXPOSE 9580

ENV JAR=${IMG_SERVICE_DIR}/${JAR_FILE}
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ${JAR}" ]
