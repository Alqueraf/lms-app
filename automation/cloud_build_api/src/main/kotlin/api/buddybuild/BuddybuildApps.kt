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

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import util.startDateWithinRange
import java.time.ZonedDateTime

object BuddybuildApps {

    interface ApiService {
        @GET("apps")
        fun getApps(): Call<List<BuddybuildAppObject>>

        // limit - how many builds to fetch. max value 100
        @GET("apps/{appId}/builds")
        fun getBuilds(@Path("appId") appId: String,
                      @Query("limit") limit: Int = resultsLimit): Call<List<BuddybuildBuildObject>>

        @POST("apps/{appId}/build")
        fun triggerBuild(@Path("appId") appId: String): Call<BuddybuildBuildTrigger>

        @GET("builds/{buildId}/tests?showFailing=true&showPassing=true")
        fun getTestResults(@Path("buildId") buildId: String): Call<BuddybuildTestResults>
    }

    private const val resultsLimit = 100

    private val apiService: ApiService by lazy {
        BuddybuildRestAdapter.retrofit.create(ApiService::class.java)
    }

    private fun <T> nullGuard(method: Call<List<T>>): List<T> {
        var body: List<T> = listOf()

        // body is null when Buddybuild has an internal server error.
        // Retrying does not help
        val result = method.execute().body()
        if (result != null) body = result

        return body
    }

    // get all apps in the token's org. unlike bitrise, a buddybuild token works for only one org.
    fun getApps(): List<BuddybuildAppObject> {
        // Remove practice apps on Buddybuild from the reports
        return nullGuard(apiService.getApps())
                .filterNot { it.app_name.contains("Practice") }
    }

    // limitResults - limit results to the first x builds
    fun getBuilds(app: BuddybuildAppObject,
                  limitDateAfter: ZonedDateTime? = null,
                  limitDateBefore: ZonedDateTime? = null,
                  limitResults: Int = resultsLimit): List<BuddybuildBuildObject> {
        val appId = app._id
        val buildsList = ArrayList<BuddybuildBuildObject>()
        val body = nullGuard(apiService.getBuilds(appId, limitResults))

        body.filterTo(buildsList) {
            startDateWithinRange(it, limitDateAfter, limitDateBefore)
        }

        return buildsList
    }

    fun triggerBuild(app: BuddybuildAppObject): BuddybuildBuildTrigger {
        return apiService.triggerBuild(app._id).execute().body()
    }

    fun getTestResults(buildId: String): BuddybuildTestResults {
        return apiService.getTestResults(buildId).execute().body()
    }
}
