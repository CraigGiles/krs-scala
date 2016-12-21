package krs.api

import com.twitter.util.{ Future }
import krs.thriftscala.{ PartnerService, PartnerOffer, OfferResponse }

import krs.domain.{ PartnerRepository }

class PartnerServerImpl(partnerRepository: PartnerRepository) {
  def apply() = {
    new PartnerService[Future] {
      def getOffers() = {
        val partnerOffers = partnerRepository.loadOffers().map((offer) => {
          PartnerOffer(
            offer.provider,
            Option(offer.creditScoreRange.min),
            Option(offer.creditScoreRange.max))
        })
        Future.value(OfferResponse(partnerOffers))
      }
    }
  }
}
