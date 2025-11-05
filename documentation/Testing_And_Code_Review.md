# Testing and Code Review

## 1. Change History

| **Change Date**   | **Modified Sections** | **Rationale** |
| ----------------- | --------------------- | ------------- |
| _Nothing to show_ |

---

## 2. Back-end Test Specification: APIs

### 2.1. Locations of Back-end Tests and Instructions to Run Them

#### 2.1.1. Tests

| **Interface**                       | **Describe Group Location, No Mocks**                        | **Describe Group Location, With Mocks**                   | **Mocked Components**        |
| ----------------------------------- | ------------------------------------------------------------ | --------------------------------------------------------- | ---------------------------- |
| **GET /api/tickets/user/:userId**   | `tests/unmocked/bingo-tickets/get-user-tickets.test.ts`      | `tests/mocked/bingo-tickets/get-user-tickets-M.test.ts`   | Ticket DB, User DB |
| **GET /api/tickets/:id**            | `tests/unmocked/bingo-tickets/get-ticket-by-id.test.ts`      | `tests/mocked/bingo-tickets/get-ticket-by-id-M.test.ts`   | Ticket DB, User DB |
| **POST /api/tickets**               | `tests/unmocked/bingo-tickets/post-bingo-tickets-NM.test.ts` | `tests/mocked/bingo-tickets/post-bingo-tickets-M.test.ts` | Ticket DB, User DB |
| **PUT /api/tickets/:id/crossedOff** | `tests/unmocked/bingo-tickets/update-crossed-off.test.ts`    | `tests/mocked/bingo-tickets/update-crossed-off-M.test.ts` | Ticket DB, User DB |
| **DELETE /api/tickets/:id**         | `tests/unmocked/bingo-tickets/delete-ticket.test.ts`         | `tests/mocked/bingo-tickets/delete-ticket-M.test.ts`      | Ticket DB, User DB |
| **POST /api/friends/send**          | `tests/unmocked/friends/send-friend-request.test.ts`         | `tests/mocked/friends/send-friend-request-M.test.ts`      | Friend DB, User DB |
| **POST /api/friends/accept**        | `tests/unmocked/friends/accept-friend-request.test.ts`       | `tests/mocked/friends/accept-friend-request-M.test.ts`    | Friend DB, User DB |
| **POST /api/friends/reject**        | `tests/unmocked/friends/reject-friend-request.test.ts`       | `tests/mocked/friends/reject-friend-request-M.test.ts`    | Friend DB, User DB |
| **GET /api/friends/list**           | `tests/unmocked/friends/get-friends.test.ts`                 | `tests/mocked/friends/get-friends-M.test.ts`              | Friend DB, User DB |
| **GET /api/friends/pending**        | `tests/unmocked/friends/get-pending-requests.test.ts`        | `tests/mocked/friends/get-pending-requests-M.test.ts`     | Friend DB, User DB |
| **DELETE /api/friends/:friendId**   | `tests/unmocked/friends/remove-friend.test.ts`               | `tests/mocked/friends/remove-friend-M.test.ts`            | Friend DB, User DB |
| **NHLService.getGameStatus()**      | `tests/unmocked/nhl-service/get-game-status.test.ts`         | `tests/mocked/nhl-service/get-game-status-M.test.ts`      | Axios, NHL API               |

#### 2.1.2. Commit Hash Where Tests Run

`[Insert Commit SHA here]`

#### 2.1.3. Explanation on How to Run the Tests

1. **Clone the Repository**:

   - Open your terminal and run:
     ```
     git clone https://github.com/cpen-321-rink-rivals/M2-hockey-prediction-app.git
     ```

2. **Navigate to the backend**

   ```
    cd backend
   ```

3. Run the tests with coverage

   ```
     npm test -- --coverage
   ```

### 2.2. GitHub Actions Configuration Location

`~/.github/workflows/backend-tests.yml`

### 2.3. Jest Coverage Report Screenshots for Tests Without Mocking

![Jest Coverage Without Mocking](/documentation/images/jest-coverage-without-mocking.png)

### 2.4. Jest Coverage Report Screenshots for Tests With Mocking

![Jest Coverage With Mocking](/documentation/images/jest-coverage-with-mocking.png)

### 2.5. Jest Coverage Report Screenshots for Both Tests With and Without Mocking

![Jest Coverage Combined](/documentation/images/jest-coverage-both.png)


---

## 3. Back-end Test Specification: Tests of Non-Functional Requirements

#### This part of the testing was skipped due to reduced team sized as discussed with the professor

---

## 4. Front-end Test Specification

### 4.1. Location in Git of Front-end Test Suite:

`frontend/src/androidTest/java/com/studygroupfinder/`

### 4.2. Tests

- **Use Case: Login**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. The user opens "Add Todo Items" screen. | Open "Add Todo Items" screen. |
    | 2. The app shows an input text field and an "Add" button. The add button is disabled. | Check that the text field is present on screen.<br>Check that the button labelled "Add" is present on screen.<br>Check that the "Add" button is disabled. |
    | 3a. The user inputs an ill-formatted string. | Input "_^_^^OQ#$" in the text field. |
    | 3a1. The app displays an error message prompting the user for the expected format. | Check that a dialog is opened with the text: "Please use only alphanumeric characters ". |
    | 3. The user inputs a new item for the list and the add button becomes enabled. | Input "buy milk" in the text field.<br>Check that the button labelled "add" is enabled. |
    | 4. The user presses the "Add" button. | Click the button labelled "add ". |
    | 5. The screen refreshes and the new item is at the bottom of the todo list. | Check that a text box with the text "buy milk" is present on screen.<br>Input "buy chocolate" in the text field.<br>Click the button labelled "add".<br>Check that two text boxes are present on the screen with "buy milk" on top and "buy chocolate" at the bottom. |
    | 5a. The list exceeds the maximum todo-list size. | Repeat steps 3 to 5 ten times.<br>Check that a dialog is opened with the text: "You have too many items, try completing one first". |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **Use Case: ...**

  - **Expected Behaviors:**

    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | ...                | ...                 |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **...**

---

## 5. Automated Code Review Results

### 5.1. Commit Hash Where Codacy Ran

`[Insert Commit SHA here]`

### 5.2. Unfixed Issues per Codacy Category

_(Placeholder for screenshots of Codacy's Category Breakdown table in Overview)_

### 5.3. Unfixed Issues per Codacy Code Pattern

_(Placeholder for screenshots of Codacy's Issues page)_

### 5.4. Justifications for Unfixed Issues

- **Code Pattern: [Usage of Deprecated Modules](#)**

  1. **Issue**

     - **Location in Git:** [`src/services/chatService.js#L31`](#)
     - **Justification:** ...

  2. ...

- ...
