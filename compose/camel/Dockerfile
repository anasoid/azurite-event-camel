FROM jbangdev/jbang-action:0.126.1

RUN jbang trust add https://github.com/apache/camel/

RUN jbang alias list

RUN jbang --verbose jdk  install 17
RUN jbang --verbose app   install camel@apache/camel

ENV PATH="${PATH}:/jbang/.jbang/bin"

RUN camel --version

ENTRYPOINT ["camel"]