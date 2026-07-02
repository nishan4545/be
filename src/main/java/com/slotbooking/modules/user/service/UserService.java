package com.slotbooking.modules.user.service;

import com.slotbooking.modules.user.dto.*;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining operations for user management.
 * Provides APIs for admin management of players and players' profile/bank details.
 */
public interface UserService {

    /**
     * Retrieves a paginated list of players based on search criteria and filters.
     *
     * @param search       optional search keyword (matches name, mobile, or email)
     * @param status       optional user status filter
     * @param role         optional user role filter
     * @param pageable     pagination request parameters
     * @return a page of user summary responses
     */
    Page<UserSummaryResponse> getPlayerList(
            String search,
            UserStatus status,
            Role role,
            Pageable pageable
    );

    /**
     * Retrieves the complete profile details of a user by their ID.
     * Accessible by admins.
     *
     * @param id the unique user identifier
     * @return the detailed user details response
     */
    UserDetailsResponse getPlayerDetails(Long id);

    /**
     * Approves a pending user registration.
     *
     * @param id    the user ID to approve
     * @param admin the current admin user performing the action
     */
    void approvePlayer(Long id, User admin);

    /**
     * Rejects a user registration.
     *
     * @param id the user ID to reject
     */
    void rejectPlayer(Long id);

    /**
     * Blocks a user from logging in.
     *
     * @param id the user ID to block
     */
    void blockPlayer(Long id);

    /**
     * Unblocks a blocked user, returning their status to APPROVED.
     *
     * @param id the user ID to unblock
     */
    void unblockPlayer(Long id);

    /**
     * Retrieves the profile details of the current logged-in user.
     *
     * @param currentUser the currently authenticated user
     * @return the detailed user profile details
     */
    UserDetailsResponse getProfile(User currentUser);

    /**
     * Updates the profile of the current logged-in user.
     * Allows updating fullName, email, and profile photo.
     *
     * @param currentUser the currently authenticated user
     * @param request     the details to update
     * @return the updated user details response
     */
    UserDetailsResponse updateProfile(User currentUser, UserProfileUpdateRequest request);

    /**
     * Adds bank details for the current user.
     *
     * @param currentUser the currently authenticated user
     * @param request     bank details to add
     * @return the created/updated bank account response
     */
    BankAccountResponse addBankDetails(User currentUser, BankDetailsRequest request);

    /**
     * Adds UPI details for the current user.
     *
     * @param currentUser the currently authenticated user
     * @param request     UPI details to add
     * @return the created/updated bank account response
     */
    BankAccountResponse addUpiDetails(User currentUser, UpiDetailsRequest request);

    /**
     * Updates the bank details of an existing bank account record.
     *
     * @param currentUser the currently authenticated user
     * @param id          the bank account ID to update
     * @param request     updated bank details
     * @return the updated bank account response
     */
    BankAccountResponse updateBankDetails(User currentUser, Long id, BankDetailsRequest request);

    /**
     * Deletes or clears the bank details of a bank account record.
     *
     * @param currentUser the currently authenticated user
     * @param id          the bank account ID to delete
     */
    void deleteBankDetails(User currentUser, Long id);

    /**
     * Retrieves the bank details for the current user.
     *
     * @param currentUser the currently authenticated user
     * @return the bank account response if it exists, or empty properties
     */
    BankAccountResponse getBankDetails(User currentUser);
}
