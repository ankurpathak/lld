Ice Connectivity Options:
# GCP to ICE Link – Connectivity Options

## 1. Purpose

This document describes the connectivity options for connecting applications running on Google Cloud Platform (GCP), including Google Kubernetes Engine (GKE), to ICE Link.

### Terminology

| Term | Description |
|---|---|
| ICE Link | ICE post-trade connectivity and middleware service |
| ICE Link AP | ICE Link Access Point / endpoint provisioned for the customer |
| ICE Global Network (IGN) | ICE network infrastructure used to provide connectivity to ICE services |
| ICE VPN Access Point | VPN termination/access point into ICE Global Network |
| ICE Cloud Connect | ICE service for connectivity between public clouds and ICE services |
| Megaport | One of the SDN/connectivity providers used by ICE Cloud Connect |

> IMPORTANT: The existence of a connectivity option within ICE Global Network does not automatically mean that the option is available for a specific ICE Link AP. ICE must confirm which connectivity methods are supported for the customer's ICE Link service.


## 2. Connectivity Options Summary

| Option | GCP to ICE Path | Public Internet | Private/Encrypted | GCP Fit |
|---|---|---|---|---|
| Direct Internet | GKE → Cloud NAT → Internet → ICE Link | Yes | TLS/Application Security | High |
| IGN VPN/IPsec | GKE → HA VPN → Internet → ICE VPN AP → IGN → ICE Link | Underlying Transport | IPsec | High |
| Cloud Connect/Megaport | GKE → GCP → Interconnect → Megaport → ICE → ICE Link | No* | Private | High |
| Dedicated Fiber | GCP → Interconnect → Carrier → ICE → IGN → ICE Link | No | Private | Medium |
| HA Direct Connect | GCP/Carrier → ICE Direct Connect → IGN → ICE Link | No | Private | Medium |
| VCC/MPLS | GCP/Carrier → MPLS L2VPN → ICE → IGN | No | Private | Medium |
| Optic Access | Carrier → Optical Connection → IGN | No | Private | Low |
| Colocation | Network → ICE Colo → Cross-connect → IGN | No | Private | Low |
| Wireless | Physical Network → ICE Wireless → IGN | No | Private | Low |

*Subject to the particular ICE Cloud Connect service architecture.


# 3. Option 1 – Direct Public Internet

## Architecture

```text
GKE Pod
   |
   v
GCP VPC
   |
   v
Cloud NAT
   |
   v
Public Internet
   |
   v
ICE Link Internet Endpoint
   |
   v
ICE Link
```

## Description

If ICE provisions the ICE Link API endpoint for Internet connectivity, the GKE application can establish outbound connectivity directly to the ICE Link endpoint.

Typical GCP components:

* GKE
* VPC
* Cloud NAT
* Static external NAT IP
* Firewall rules
* TLS/application authentication
* Secret Manager for credentials/certificates

## Advantages

* Simplest architecture
* Lowest network complexity
* No VPN infrastructure
* No dedicated circuit
* No Megaport requirement
* Fast deployment

## Considerations

* Traffic traverses the public Internet
* Internet routing is not deterministic
* ICE may require source-IP allowlisting
* TLS and ICE application security requirements must be implemented
* Production/DR endpoints should be confirmed with ICE

## ICE Documentation

ICE Link:
https://www.ice.com/ice-repository-and-confirmation-services/ice-link


# 4. Option 2 – ICE Global Network VPN / IPsec

## Architecture

```text
GKE Pod
   |
   v
GCP VPC
   |
   v
Google Cloud HA VPN
   |
   | IPsec
   v
Public Internet
   |
   v
ICE VPN Access Point
   |
   v
ICE Global Network
   |
   v
ICE Link
```

## Description

ICE Global Network provides IP-based VPN access.

The public Internet provides the underlying network transport, while traffic between GCP and ICE is carried through an encrypted IPsec tunnel.

On GCP, Google Cloud HA VPN can potentially provide the customer-side IPsec termination.

## Typical Network Requirements

Depending on the ICE-provided configuration:

* Static public IP
* IKE/IPsec
* UDP 500
* NAT-T / UDP 4500
* ESP
* ICE-assigned IP addressing
* Routing configuration
* Redundant tunnels where required

## Advantages

* Encrypted IPsec connectivity
* Does not require a dedicated physical circuit
* Good fit with GCP HA VPN
* Redundancy can be implemented

## Considerations

* Internet is still the underlying transport
* Requires VPN configuration
* Requires coordination with ICE
* Routing and NAT requirements must match ICE specifications

## ICE Documentation

ICE Global Network – US Technical Specifications:

https://www.ice.com/publicdocs/IGN_Colocation_US_Technical_Specifications.pdf

The ICE technical documentation identifies:

"ICE Global Network VPN Access – IP based VPN"


# 5. Option 3 – ICE Cloud Connect / Megaport

## Architecture

```text
GKE
 |
 v
GCP VPC
 |
 v
Cloud Router
 |
 v
Google Cloud Interconnect
 |
 v
Megaport
 |
 v
ICE Cloud Connect
 |
 v
ICE Network / Service
 |
 v
ICE Link
```

## Description

ICE Cloud Connect provides connectivity between public-cloud environments and selected ICE services.

ICE explicitly lists Google Cloud as a supported cloud environment.

ICE also identifies Megaport and Zenlayer as SDN providers used for Cloud Connect.

## Potential GCP Components

* GKE
* GCP VPC
* Cloud Router
* Partner/Dedicated Interconnect
* BGP
* Megaport connectivity
* ICE Cloud Connect

## Advantages

* Private cloud connectivity
* Avoids normal Internet routing
* Google Cloud supported by ICE Cloud Connect
* Designed for cloud connectivity
* Redundant cloud connectivity can potentially be provisioned

## Considerations

* More complex than Internet connectivity
* Additional ICE/provider costs
* Requires Megaport/ICE provisioning
* Requires routing/BGP design
* ICE Link availability over Cloud Connect must be explicitly confirmed

> IMPORTANT: ICE's Cloud Connect documentation demonstrates that Google Cloud connectivity exists. It does not by itself prove that every ICE Link AP is available through Cloud Connect.

## ICE Documentation

ICE Cloud Connect:

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/cloud-connect


# 6. Option 4 – Dedicated Fiber / Private Circuit

## Architecture

```text
GKE
 |
 v
GCP VPC
 |
 v
Google Cloud Interconnect
 |
 v
Network Carrier
 |
 v
ICE Global Network Access Center
 |
 v
ICE Global Network
 |
 v
ICE Link
```

## Description

ICE Global Network provides dedicated fiber connectivity through optimized fiber routes, point-to-point circuits and ICE Global Network Access Centers.

For GCP, connectivity would generally involve Cloud Interconnect and a carrier/network provider capable of reaching ICE infrastructure.

## Advantages

* Dedicated private connectivity
* Predictable network path
* No public Internet dependency
* Enterprise-grade connectivity
* Redundancy can be designed

## Considerations

* Higher cost
* Carrier involvement
* Longer provisioning time
* More complex network architecture
* Requires coordination between Google Cloud, carrier and ICE

## ICE Documentation

ICE Fiber Networks:

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/fiber-networks


# 7. Option 5 – HA Direct Connect

## Architecture

```text
GCP / Customer Network
        |
        v
Carrier / Connectivity Provider
        |
        v
ICE HA Direct Connect
        |
        v
ICE Global Network
        |
        v
ICE Link / ICE Service
```

## Description

ICE technical documentation identifies HA Direct Connect (SDC) as an ICE Global Network high-availability connectivity service.

## GCP Consideration

A GCP implementation would generally require an appropriate carrier/private connectivity architecture between Google Cloud and the ICE access location.

The exact architecture must be confirmed with ICE.

## ICE Documentation

ICE Global Network – US Technical Specifications:

https://www.ice.com/publicdocs/IGN_Colocation_US_Technical_Specifications.pdf


# 8. Option 6 – HA Virtual Control Circuit (VCC)

## Architecture

```text
GCP / Customer Network
        |
        v
Network Provider
        |
        v
MPLS L2VPN
        |
        v
ICE VCC
        |
        v
ICE Global Network
        |
        v
ICE Service
```

## Description

ICE documentation identifies HA Virtual Control Circuit (VCC) as an MPLS-based Layer-2 VPN connectivity service.

## GCP Consideration

This is not a native GKE connectivity mechanism.

An appropriate network/carrier provider would normally be required to integrate GCP connectivity with the ICE VCC service.

## ICE Documentation

ICE Global Network – US Technical Specifications:

https://www.ice.com/publicdocs/IGN_Colocation_US_Technical_Specifications.pdf


# 9. Option 7 – ICE Global Network Optic Access

## Architecture

```text
Customer / Carrier
       |
       | Dedicated Optical Connection
       v
ICE Optic Access
       |
       v
ICE Global Network
       |
       v
ICE Service
```

## Description

ICE technical documentation identifies ICE Global Network Optic Access as another network access service.

It is primarily relevant to customers requiring dedicated high-performance physical connectivity.

## GCP Consideration

This is generally not a direct GKE connectivity option.

A carrier/physical network presence would be required.

## ICE Documentation

ICE Global Network Technical Specifications:

https://www.ice.com/publicdocs/IGN_Colocation_US_Technical_Specifications.pdf

ICE Fiber Networks:

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/fiber-networks


# 10. Option 8 – ICE Colocation / Cross-Connect

## Architecture

```text
GCP / Customer Network
        |
        v
Carrier
        |
        v
ICE Colocation Facility
        |
        v
Physical Cross-Connect
        |
        v
ICE Global Network
        |
        v
ICE Service
```

## Description

ICE provides colocation infrastructure where customers can establish physical connectivity to ICE Global Network and associated services.

## Typical Use Cases

* Trading infrastructure
* Existing financial-market colocation
* Very low-latency requirements
* Physical infrastructure close to ICE

## GCP Consideration

This is generally not the first choice for an application already hosted entirely within GKE.

## ICE Documentation

ICE Colocation:

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/icecolocation


# 11. Option 9 – Wireless Connectivity

## Architecture

```text
Customer / Trading Location
        |
        v
ICE Wireless Network
        |
        v
ICE Global Network
        |
        v
ICE Service
```

## Description

ICE provides specialized wireless network connectivity between selected financial-market locations.

This is primarily designed for specialized low-latency financial-market connectivity.

## GCP Consideration

This is generally not a direct GCP-to-ICE Link architecture.

## ICE Documentation

ICE Wireless Networks:

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/wireless


# 12. ICE Global Network – Conceptual Architecture

ICE Global Network should not be confused with an ICE VPN Access Point or ICE Link itself.

```text
                         ICE Link
                            |
                            v
                       ICE Link AP
                            |
                            v
                   ICE Global Network
                            ^
                            |
          +-----------------+------------------+
          |                 |                  |
          |                 |                  |
      VPN Access       Cloud Connect      Private Circuit
          |                 |                  |
       IPsec             Megaport            Carrier
          |                 |                  |
       Internet             |                  |
          |                 |                  |
          +-----------------+------------------+
                            |
                            v
                           GCP
                            |
                            v
                           GKE
```

The layers are therefore:

```text
Application Layer
      |
      v
ICE Link
      |
      v
ICE Link AP
      |
      v
ICE Network
      |
      v
Connectivity Method
      |
      v
GCP Network
      |
      v
GKE Application
```


# 13. Recommended GCP Options

For a GKE application, the first three connectivity models that should be evaluated are:

## Option A – Direct Internet

```text
GKE
 |
 v
VPC
 |
 v
Cloud NAT
 |
 v
Internet
 |
 v
ICE Link
```

Use when ICE confirms that the ICE Link endpoint is directly Internet accessible.

Complexity: LOW


## Option B – IGN VPN

```text
GKE
 |
 v
VPC
 |
 v
GCP HA VPN
 |
 | IPsec
 v
Internet
 |
 v
ICE VPN Access Point
 |
 v
ICE Global Network
 |
 v
ICE Link
```

Use when ICE provisions IGN VPN connectivity for ICE Link.

Complexity: MEDIUM


## Option C – Cloud Connect / Megaport

```text
GKE
 |
 v
VPC
 |
 v
Cloud Router
 |
 v
Cloud Interconnect
 |
 v
Megaport
 |
 v
ICE Cloud Connect
 |
 v
ICE Network
 |
 v
ICE Link
```

Use when ICE confirms that the ICE Link service/AP is available through Cloud Connect.

Complexity: MEDIUM/HIGH


## Option D – Dedicated Private Circuit

```text
GKE
 |
 v
GCP
 |
 v
Cloud Interconnect
 |
 v
Carrier
 |
 v
ICE Access Center
 |
 v
ICE Global Network
 |
 v
ICE Link
```

Use when dedicated private connectivity is required.

Complexity: HIGH


# 14. Decision Tree

```text
                    GKE → ICE Link
                          |
                          v
              Is ICE Link endpoint
               Internet accessible?
                    /           \
                  YES            NO
                   |              |
                   v              v
               INTERNET      Is IGN VPN
                              available?
                              /       \
                            YES        NO
                             |          |
                             v          v
                         GCP HA VPN   Is Cloud Connect
                                       available for
                                       ICE Link?
                                      /        \
                                    YES         NO
                                     |           |
                                     v           v
                                  Megaport    Dedicated
                                  / Cloud      Private
                                  Connect      Circuit
```


# 15. Questions to Confirm With ICE

Before selecting the final architecture, confirm the following with ICE:

1. What connectivity type is provisioned for our ICE Link AP?
2. Is our ICE Link AP directly reachable over the public Internet?
3. Is our ICE Link AP reachable through ICE Global Network VPN?
4. Is our ICE Link AP reachable through ICE Cloud Connect/Megaport?
5. Is dedicated private/fiber connectivity available for ICE Link?
6. What are the production ICE Link AP DNS names/IP addresses?
7. What are the DR ICE Link AP DNS names/IP addresses?
8. What TCP ports are required?
9. Is source-IP allowlisting required?
10. Is TLS required?
11. Is client-certificate authentication required?
12. What source/destination IP ranges are required?
13. Is BGP required for private connectivity?
14. What routing model should be used?
15. What redundancy architecture does ICE recommend?
16. Is active/active connectivity supported?
17. Is active/standby connectivity supported?
18. What is ICE's recommended failover mechanism?


# 16. ICE Documentation References

## ICE Link

https://www.ice.com/ice-repository-and-confirmation-services/ice-link

## ICE Global Network

https://www.ice.com/fixed-income-data-services/access-and-delivery/global-network

## ICE Global Network Documents

https://www.ice.com/fixed-income-data-services/access-and-delivery/global-network/documents

## ICE Cloud Connect

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/cloud-connect

## ICE Fiber Networks

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/fiber-networks

## ICE Colocation

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/icecolocation

## ICE Wireless Networks

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/wireless

## ICE Hosting & Managed Services

https://www.ice.com/fixed-income-data-services/access-and-delivery/connectivity-and-feeds/hosting-managed-services

## ICE Global Network – US Technical Specifications

https://www.ice.com/publicdocs/IGN_Colocation_US_Technical_Specifications.pdf


# 17. Conclusion

For GCP/GKE to ICE Link, the primary connectivity options to evaluate are:

1. Direct Internet
2. ICE Global Network IPsec VPN
3. ICE Cloud Connect / Megaport
4. Dedicated private connectivity

The recommended starting point is to ask ICE which of these connectivity methods is supported for the specific ICE Link AP provisioned to the organization.

The final architecture should be selected only after ICE confirms the supported network path, production/DR endpoints, routing, security, authentication and resiliency requirements.
