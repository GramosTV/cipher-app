package com.example.cipher.data.network.api

import com.example.cipher.data.network.dto.ContactActionDto
import com.example.cipher.data.network.dto.ContactListDto
import com.example.cipher.data.network.dto.ContactRequestDto
import com.example.cipher.data.network.dto.ContactResponseDto
import retrofit2.http.*

interface ContactApiService {
    
    @POST("api/contacts/request")
    suspend fun sendContactRequest(@Body request: ContactRequestDto): ContactResponseDto
    
    @GET("api/contacts")
    suspend fun getContacts(): ContactListDto
    
    @GET("api/contacts/pending-requests")
    suspend fun getPendingRequests(): ContactListDto
    
    @PUT("api/contacts/action")
    suspend fun handleContactAction(@Body action: ContactActionDto): ContactResponseDto
    
    @PUT("api/contacts/{id}")
    suspend fun updateDisplayName(
        @Path("id") contactId: Long,
        @Body displayName: Map<String, String>
    ): ContactResponseDto
    
    @DELETE("api/contacts/{id}")
    suspend fun deleteContact(@Path("id") contactId: Long)
}
