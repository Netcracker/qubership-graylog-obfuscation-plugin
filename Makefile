SHELL := /usr/bin/env bash

JDK17_HOME ?= /usr/lib/jvm/java-17-openjdk
MAVEN_REPO_LOCAL ?= $(CURDIR)/.m2/repository
GRAYLOG_VERSION ?= $(shell sed -n 's:.*<graylog.version>\(.*\)</graylog.version>.*:\1:p' pom.xml | head -n 1)
GRAYLOG_WEB_SRC ?= $(CURDIR)/.graylog/graylog2-server/graylog2-web-interface
MAVEN := mvn -Dmaven.repo.local=$(MAVEN_REPO_LOCAL)

ifneq ($(wildcard $(JDK17_HOME)/bin/java),)
export JAVA_HOME := $(JDK17_HOME)
export PATH := $(JAVA_HOME)/bin:$(PATH)
endif

.PHONY: backend-test backend-package graylog-web frontend-install frontend-test frontend-build package smoke clean

backend-test:
	$(MAVEN) -Dskip.web=true test

backend-package:
	$(MAVEN) -Dskip.web=true package

graylog-web:
	GRAYLOG_VERSION=$(GRAYLOG_VERSION) scripts/update-graylog-web.sh

frontend-install: graylog-web
	yarn install --frozen-lockfile

frontend-test: frontend-install
	yarn test

frontend-build: frontend-install
	GRAYLOG_WEB_SRC=$(GRAYLOG_WEB_SRC) yarn build

package:
	$(MAVEN) -Dskip.web=true clean
	$(MAKE) frontend-build
	$(MAVEN) -Dskip.web=true package

smoke:
	scripts/graylog-smoke-test.sh

clean:
	$(MAVEN) clean
