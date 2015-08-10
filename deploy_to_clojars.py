import urllib2
import json
from subprocess import call

project_clj_version = open('project.clj').readlines()[0].split()[-1][1:-1]
print "project_clj_version: %s" % project_clj_version

clojars_latest_version = json.loads(urllib2.urlopen("https://clojars.org/api/artifacts/traduki").read())['latest_version']
print "clojars latest version: %s" % clojars_latest_version


if project_clj_version > clojars_latest_version:
    print "Deploying to clojars: "
    call(["lein", "deploy", "clojars"])
    print "Deployment complete :)"
else:
    print "Not deploying: Everything up to date"
