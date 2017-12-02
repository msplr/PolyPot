FROM alpine:3.6

ENV POLYPOT_SETTINGS=config-prod.py GID=1000 UID=1000

RUN BUILD_DEPS="python-dev" \
 && apk -U add \
    $BUILD_DEPS \
    py-pip \
    py-enum34 \
    su-exec

COPY app /polypot
COPY docker/run.sh /polypot/run.sh
RUN chmod +x /polypot/run.sh

RUN pip install -r /polypot/requirements.txt \
 && apk del $BUILD_DEPS \
 && rm -f /var/cache/apk/*

VOLUME /polypot/db

EXPOSE 5000

CMD ["/polypot/run.sh"]
