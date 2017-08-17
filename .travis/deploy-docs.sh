#!/bin/bash -e

stop() {
  echo $* >&2
  exit 0
}

DOCUMENTATION_BRANCHES=(
  "documentation/docs-via-swagger-jekyll" \
  "develop" \
  "master" \
  "versions/1.10.x"
)

deploy_on_ghpages() {
  var="$1"
  for item in ${DOCUMENTATION_BRANCHES[@]} ; do
      [ "$var" == "$item" ] && return 0
  done
  return 1
}

[ !$(deploy_on_ghpages "${TRAVIS_BRANCH}") ] \
  || stop "do not deploy docs of ${TRAVIS_BRANCH}"

[ "${TRAVIS_SECURE_ENV_VARS}" == "true" ] \
  || stop "no secure enviroment variables were provided"

[ "${TRAVIS_JOB_NUMBER}" == "${TRAVIS_BUILD_NUMBER}.1" ] \
  || stop "only the first build job will be deployed"

[ "${TRAVIS_PULL_REQUEST}" == "false" ] \
  || stop "no deployment for pull requests"

github_name="Travis CI"
github_mail="travis@travis-ci.org"
branch="gh-pages"
build_dir=$(mktemp -d)
#docs_dir="${build_dir}/docs"
docs_dir="./web-resources/src/main/docs"
docs_deploy_dir="${docs_dir}/_site"

git config --global user.name "${github_name}"
git config --global user.email "${github_mail}"
git clone --quiet --depth 1 --branch "${branch}" https://github.com/${TRAVIS_REPO_SLUG}.git "${build_dir}"

output_folder=$(echo $TRAVIS_BRANCH | \sed 's/\//_/')

pushd "${docs_dir}"
echo "Current directory `pwd`"
bundle update
bundle exec jekyll build --baseurl "/series-rest-api/${output_folder}"
bundle exec htmlproofer "./site"
popd

rm -rf "${build_dir:?}/${output_folder}"
cp -rv "${docs_deploy_dir}" "${build_dir}/${output_folder}"

pushd "${build_dir}"
git add --ignore-removal .
git add --update :/
git commit -m "Updating ${TRAVIS_BRANCH} on ${branch} to ${TRAVIS_COMMIT}"
git push --force --quiet "https://${GITHUB_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git" "${branch}" >/dev/null 2>&1
popd
