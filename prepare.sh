#! /bin/bash
set -euxo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

rm -rf esbuild-bin
mkdir -p esbuild-bin
(
    cd esbuild-bin
    npm i @esbuild/wasi-preview1
)

rm -rf esbuild-test
mkdir -p esbuild-test
(
    cd esbuild-test
    npm install react react-dom
)

cat << EOF >> esbuild-test/app.jsx
import * as React from 'react'
import * as Server from 'react-dom/server'

let Greet = () => <h1>Hello, world!</h1>
console.log(Server.renderToString(<Greet />))
EOF

rm -f src/main/resources/esbuild.wasm
cp esbuild-bin/node_modules/@esbuild/wasi-preview1/esbuild.wasm src/main/resources

(
    cd esbuild-test
    npm install --save-exact --save-dev esbuild
    ./node_modules/.bin/esbuild app.jsx --bundle --outfile=result.js
)
