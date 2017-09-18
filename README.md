# Micro RSVP
Backend for managing RSVP transaction states.

* [Domain](#domain)
  * [States](#states)
* [API](#api)
  * [POST /invitee/new](#post-inviteenew)
  * [GET /invitee/all](#get-inviteeall)
  * [GET /invitee/`<code>`](#get-inviteecode)
  * [PUT /invitee/`<code>`/metadata](#put-inviteecodemetadata)
  * [PUT /invitee/`<code>`/rsvp](#put-inviteecodersvp)
  * [PUT /invitee/`<code>`/details](#put-inviteecodedetails)
  * [PUT /invitee/`<code>`/additional-invitees](#put-inviteecodeadditional-invitees)
  * [PUT /invitee/`<code>`/optional-info](#put-inviteecodeoptional-info)
## Domain
Each user is uniquely identified by the code given to the user.  The must enter the correct 4-digit code to be able to RSVP for the event. Once the user confirms his/her RSVP status, a form must be filled.

### States
The possible states for an invitee are:

#### UNVERIFIED
*Trigger*: when entered into the designated Google Sheet, Zapier calls the endpoint that initializes an invitee instance.

#### VERIFIED
*Trigger*: when user enters the valid code.

#### RESPONDED
*Trigger*: when the user responds to the invitation with `YES`, `NO` or `MAYBE`.

#### FORM_SUBMITTED
*Trigger*: when the user submits the form.


## API

For all endpoints, we return a status of `500` with `{ "status": "AWS_ERROR" }` in the case of an error calling AWS.
### POST /invitee/new

Insert a new invitee into the db. Requires an auth key. In case an invitee with the specified code exists, it will be _replaced_.
#### Request
```
URL: /invitee/new
Method: POST
Headers:
	Authorization: Bearer 23mlwkejf08ads4j
Content-Type: application/json
Body: {
    name: "Alex Ferguson",
    code: "7643",
    date_given:	"Kadima K",
	given_by: "City of Scottsdale",
	pre_entered_company: "Some Company",
	pre_entered_title: "President",
	relationship: "Friend"
}
```
#### Success Response (200 OK)
```
Content-Type: application/json
Body: {
    status: "SUCCESS",
    data: {
		code: 123,
		rsvp_state: "UNVERIFIED"
	},
}
```

#### Failure Response (500 Internal Server Error)
```
Content-Type: application/json
Body: {
    status: "COULD_NOT_ADD_INVITEE",
}
```

### GET /invitee/all

Gets all invitees.

### GET /invitee/`<code>`

Given a code, checks if it is valid, and returns the host's name from the database, and the state of the passcode in the flow. If it has already been verified, then the user will be flagged in the db.

#### Request
```
URL: /invitee/1234
Method: GET
```

#### Success Response (200 OK)
**If first time**:
```
Content-Type: application/json
Body: {
    status: "SUCCESS",
	data: {
		name: "Alex Ferguson",
		code: "7643",
		dateGiven:	"",
		givenBy: "City of Scottsdale",
		company: "Some Company",
		title: "President",
		relationship: "Friend",
		originationSource: "Kadima",
		rsvpState: "VERIFIED"
	}
}
```

**If second time, having responded "Yes" or "Maybe" initially**:
```
Content-Type: application/json
Body: {
	status: "ALREADY_RESPONDED"
}
```


**If second time, having responded "No" initially**:
```
Content-Type: application/json
Body: {
    status: "SUCCESS",
	data: {
		name: "Alex Ferguson",
		code: "7643",
		date_given:	"",
		givenBy: "City of Scottsdale",
		company: "Some Company",
		title: "President",
		relationship: "Friend",
		origination_source: "Kadima",
		confirmation: "Maybe",
		rsvp_state: "RESPONDED"
	}
}
```
#### Not Found Response (404 Not Found)
```
Content-Type: application/json
Body: {
    status: "NOT_FOUND"
}
```

### PUT /invitee/`<code>`/metadata
Update the prefilled metadata of the invitation.
#### Request
```
URL: /invitee/1234/metadata
Method: PUT
Body: {
	origination_source: "Kadima",
	given_by: "Alexis K"
}
```

#### Success Response (200 OK)
```
Content-Type: application/json
Body: {
    status: "SUCCESS",
}
```

### PUT /invitee/`<code>`/rsvp
Update the RSVP status of the user.
#### Request
```
URL: /invitee/1234/rsvp
Method: PUT
Body: {
	response: "YES",
	rsvp_state: "RESPONDED"
}
```

#### Success Response (200 OK)
```
Content-Type: application/json
Body: {
    status: "SUCCESS",
}
```

### PUT /invitee/`<code>`/details

Enter and save form details entered by the user.
#### Request
```
URL: /invitee/123/details
Method: PUT
Body: {
	name: "ABC",
	company: "DEF",
	title: "VP",
	address: "1 Jl. 234",
	address_2: "2 Jl. 343",
	city: "BWI",
	state: "MD",
	zip: "20012",
	email: "x@y.com",
	mobile_number: "1233434343",
	work_phone: "123123323",
	website: "www.y.com"
}
```

#### Success Response (200 OK)
```
Content-Type: application/json
Body: {
	status: "SUCCESS",
	rsvp_state: "FORM_SUBMITTED",
}
```

### PUT /invitee/`<code>`/additional-invitees
Enter a JSON array of additional invitees.

#### Request
```
URL: /invitee/123/additional-invitees
Method: PUT
Body: [{"name": "Sh7", "email": "a@b.com"}, {"name": "Cr8", "email": "cr8@a.com"}]
```

#### Success Response (200 OK)
```
Content-Type: application/json
Body: {
	status: "SUCCESS",
}
```

### PUT /invitee/`<code>`/optional-info
#### Request
```
URL: /invitee/123/optional-info
Method: PUT
Body: {
	is_advisor: "Yes",
	is_mentor: "No",
	is_affiliated_municipality: "Yes",
	is_support_ventures: "No",
	is_interested_in_partnership: "Yes",
	is_interested_in_innovation_fair: "Yes",
	startup_phase:  "Idea",
	any_funding_yet: "No",
	industry: "Hospitality",
	incubator: "No",
	incubator_name: "NULL",
	corportate_partnerships: "No"
}
```

#### Response
```
Content-Type: application/json
Body: {
	status: "SUCCESS"
}
```
