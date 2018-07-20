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

import api.RestAdapter
import api.RestAdapterUtils.createHttpClient
import api.RestAdapterUtils.createRetrofit
import retrofit2.Retrofit
import util.getEnv

object BuddybuildRestAdapter : RestAdapter {

    override val retrofit: Retrofit by lazy {
        val buddybuildToken = getEnv("BUDDYBUILD_TOKEN")
        val baseUrl = "https://api.buddybuild.com/v1/"
        val bitriseAuthorization = "Bearer $buddybuildToken"
        val bitriseHttpClient = createHttpClient(bitriseAuthorization).build()

        createRetrofit(bitriseHttpClient, baseUrl)
    }
}
