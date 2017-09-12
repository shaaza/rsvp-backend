# Micro RSVP
Backend for managing RSVP transaction states.

* [Domain](#domain)
  * [States](#states)
* [API](#api)
  * [POST /invitee/new](#post-inviteenew)
  * [GET /invitee/`<code>`](#get-inviteecode)
  * [PUT /invitee/`<code>`/metadata](#put-inviteecodemetadata)
  * [PUT /invitee/`<code>`/rsvp](#put-inviteecodersvp)
  * [PUT /invitee/`<code>`/details](#put-inviteecodedetails)

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

### POST /invitee/new

Insert a new invitee into the db. Requires an auth key.
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
    date_given:	"",
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

#### Already Exists Response (403 Forbidden)
```
Content-Type: application/json
Body: {
    status: "CODE_ALREADY_EXISTS",
	data: {
		code: 123
	}
}
```

#### Failure Response (500 Internal Server Error)
```
Content-Type: application/json
Body: {
    status: "COULD_NOT_ADD_INVITEE",
}
```

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

**If second time, having completed some parts of the flow**:
```
Content-Type: application/json
Body: {
    status: "SUCCESS",
	data: {
		code: 123
		rsvpState: "RESPONDED"
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
URL: /invitee?code=1234
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
URL: /invitee?code=1234
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
	mobile_phone: "1233434343",
	work_phone: "123123323",
	website: "www.y.com"
}
```

#### Success Response (200 OK)
```
Content-Type: application/json
Body: {
	invitee_state: "FORM_SUBMITTED",
}
```
