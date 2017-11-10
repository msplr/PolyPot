FROM alpine:3.6

ENV POLYPOT_SETTINGS=config-prod.py

RUN BUILD_DEPS="python-dev" \
 && apk -U add \
    $BUILD_DEPS \
    py-pip

COPY app /polypot

RUN pip install -r /polypot/requirements.txt \
 && apk del $BUILD_DEPS \
 && rm -f /var/cache/apk/*

VOLUME /polypot/db

EXPOSE 5000

WORKDIR /polypot
ENTRYPOINT ["python"]
CMD ["polypot_server.py"]
