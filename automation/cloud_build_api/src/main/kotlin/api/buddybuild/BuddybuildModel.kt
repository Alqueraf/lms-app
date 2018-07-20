//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package api.buddybuild

import normal.ToNormalApp

//[
// {
//    "_id": "5d083602febef8d9db17a607",
//    "app_name": "Canvas",
//    "platform": "ios"
//},
data class BuddybuildAppObject(
        val _id: String,
        val app_name: String,
        val platform: String
) : ToNormalApp

/*
[
    {
        "_id": "1e98d16780dd72730a45d7a2",
        "app_id": "58b0b2116096900100863eb8",
        "repo_url": "https://github.com/instructure/example.git",
        "build_status": "success",
        "finished": true,
        "commit_info": {
            "tags": [],
            "branch": "mbl-8590",
            "commit_sha": "6ad88ce133dc056ffb1ed1b4088433b3a7b6ac3a",
            "html_url": "https://github.com/instructure/example/commit/6ad88ce133dc056ffb1ed1b4088433b3a7b6ac3a",
            "author": "A b",
            "message": "some git message"
        },
        "build_number": 3855,
        "created_at": "2017-10-13T20:15:39.192Z",
        "started_at": "2017-10-13T20:15:47.517Z",
        "finished_at": "2017-10-13T20:29:38.927Z",
        "test_summary": {},
        "links": {
            "download": [],
            "install": []
        },
        "scheme_name": "Example scheme"
    }
]
*/

data class BuddybuildCommitInfo(
        val tags: List<String>,
        val branch: String,
        val commit_sha: String,
        val html_url: String,
        val author: String,
        val message: String
)

data class BuddybuildNamedUrl(
        val name: String,
        val url: String
)

data class BuddybuildLinks(
        val download: List<BuddybuildNamedUrl>,
        val install: List<BuddybuildNamedUrl>
)

data class BuddybuildBuildObject(
        val _id: String,
        val app_id: String,
        val repo_url: String,
        val build_status: String,
        val finished: Boolean,
        val commit_info: BuddybuildCommitInfo,
        val build_number: Int,
        val created_at: String,
        val started_at: String,
        val finished_at: String,
        val test_summary: kotlin.Any,
        val links: BuddybuildLinks,
        val scheme_name: String
)

data class BuddybuildBuildTrigger(
        val build_id: String
)

data class BuddybuildTestResults(
        val build_id: String,
        val tests: List<BuddybuildTest>
)

data class BuddybuildTest(
        val run: String,
        val target: String,
        val suite: String,
        val test: String, // API docs say to use "name" but they're wrong :)
        val status: String
)
